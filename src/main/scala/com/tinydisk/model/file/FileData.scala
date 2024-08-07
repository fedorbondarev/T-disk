package com.tinydisk.model.file

import fs2.Stream

case class FileData[F[_]](
  name: String,
  data: Stream[F, Byte]
)
