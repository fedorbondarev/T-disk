package com.tinydisk.services.publicfile

import cats.data.{EitherT, OptionT}
import cats.effect.kernel.{Concurrent, MonadCancelThrow}
import cats.effect.std.Random
import cats.implicits.{catsSyntaxApplicativeError, toFlatMapOps, toFunctorOps}
import com.tinydisk.database.FileMetadataQueries
import com.tinydisk.database.publictoken.FilePublicTokenQueries
import com.tinydisk.error.{ApiError, BusinessApiError, ServerApiError}
import com.tinydisk.model.file.FileData
import com.tinydisk.model.publictoken
import com.tinydisk.model.publictoken.{ContentPublicToken, PublicToken}
import com.tinydisk.services.filestorage.FileStorage
import doobie.Transactor
import doobie.implicits.{toConnectionIOOps, toOptionTConnectionIOOps}
import tofu.syntax.feither.EitherFOps

import fs2.Stream

class PublicFileServiceImpl[F[_]: Random: Concurrent](
  val fileStorage: FileStorage[F],
  val transactor: Transactor[F]
) extends PublicFileService[F] {
  @inline private def genUniqueFileName: F[String] = Random[F].nextBytes(32).map(_.map("%02x".format(_)).mkString)

  override def save(fileData: FileData[F]): F[Either[ApiError, PublicToken]] =
    (for {
      uniqueName <- genUniqueFileName
      (token, savingFileRes) <- Concurrent[F].both(
        (for {
          fileId                            <- FileMetadataQueries.save(fileData.name, uniqueName)
          (_, ContentPublicToken(token, _)) <- FilePublicTokenQueries.save(fileId)
        } yield token).transact(transactor),
        fileStorage.saveFile(fileData.data, uniqueName)
      )
      _ <- MonadCancelThrow[F].fromEither(savingFileRes)
    } yield token)
      .attempt
      .leftMapIn(e => ServerApiError(e.getMessage))

  override def get(token: publictoken.PublicToken): F[Either[ApiError, (Stream[F, Byte], String)]] =
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
      .value
}
