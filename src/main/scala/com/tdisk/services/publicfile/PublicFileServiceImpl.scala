package com.tdisk.services.publicfile

import cats.data.{EitherT, OptionT}
import cats.effect.kernel.MonadCancelThrow
import cats.implicits.{catsSyntaxApplicativeError, toFlatMapOps, toFunctorOps}
import com.tdisk.database.FileMetadataQueries
import com.tdisk.database.publictoken.FilePublicTokenQueries
import com.tdisk.error.{ApiError, BusinessApiError, ServerApiError}
import com.tdisk.model.file.{FileData, FileMetadata}
import com.tdisk.model.publictoken
import com.tdisk.model.publictoken.{ContentPublicToken, PublicToken}
import com.tdisk.services.filestorage.FileStorage
import doobie.Transactor
import doobie.implicits.{toConnectionIOOps, toOptionTConnectionIOOps}
import tofu.syntax.feither.EitherFOps

import java.io.InputStream

class PublicFileServiceImpl[F[_]: MonadCancelThrow](
  val fileStorage: FileStorage[F],
  val transactor: Transactor[F]
) extends PublicFileService[F] {
  override def save(fileData: FileData): F[Either[ApiError, PublicToken]] =
    (for {
      (uniqueName, token) <- (for {
        (fileId, FileMetadata(_, uniqueName)) <- FileMetadataQueries.save(fileData.name)
        (_, ContentPublicToken(token, _))     <- FilePublicTokenQueries.save(fileId)
      } yield (uniqueName, token)).transact(transactor)
      savingFileRes <- fileStorage.saveFile(fileData.data, uniqueName)
      _             <- MonadCancelThrow[F].fromEither(savingFileRes)
    } yield token)
      .attempt
      .leftMapIn(e => ServerApiError(e.getMessage))

  override def get(token: publictoken.PublicToken): F[Either[ApiError, (InputStream, String)]] =
    (for {
      fileMetadata <-
        (for {
          fileId       <- OptionT(FilePublicTokenQueries.getFileIdByToken(token))
          fileMetadata <- OptionT(FileMetadataQueries.get(fileId))
        } yield fileMetadata)
          .transact(transactor)
          .toRight(BusinessApiError("file not found by token"))
      data <- EitherT(fileStorage.readFile(fileMetadata.uniqueName))
        .leftMap[ApiError](e => ServerApiError(e.getMessage))
    } yield (data, fileMetadata.name))
      .attempt
      .fold(Left(_), identity)
}
