package com.gu.media.upload.model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

// All data is conceptually immutable except UploadProgress
case class Upload(id: String, parts: List[UploadPart], metadata: UploadMetadata, progress: UploadProgress)

object Upload {
  implicit val format: Format[Upload] = Jsonx.formatCaseClass[Upload]

  // We want 100MB chunks. YouTube mandates that chunk size must be a multiple of 256KB
  val oneHundredMegabytes: Long = 100 * 1024 * 1024
  val twoFiveSixKilobytes: Long = 1024 * 256

  def calculateChunks(size: Long): List[(Long, Long)] = {
    chunksOfExactly(oneHundredMegabytes, size) match {
      case (bigChunks, 0) =>
        bigChunks

      case (bigChunks, remainder) =>
        chunkMultipleOf(twoFiveSixKilobytes, size - remainder, size) match {
          case ((start, end), 0) =>
            bigChunks :+ ((start, end))

          case ((start, end), remainder) =>
            bigChunks ++ List((start, end), (size - remainder, size))
        }
    }
  }

  def mergeProgress(upload: Upload, progress: UploadProgress): Upload = {
    upload.copy(progress = UploadProgress(
      uploadedToS3 = Math.max(upload.progress.uploadedToS3, progress.uploadedToS3),
      uploadedToYouTube = Math.max(upload.progress.uploadedToYouTube, progress.uploadedToYouTube)
    ))
  }

  private def chunksOfExactly(chunkSize: Long, size: Long): (List[(Long, Long)], Long) = {
    val numParts = (size / chunkSize).toInt
    val remainder = size % chunkSize

    val chunks = (0 until numParts).toList.map { n =>
      val chunkStart = n * chunkSize
      val chunkEnd = chunkStart + chunkSize

      (chunkStart, chunkEnd)
    }

    (chunks, remainder)
  }

  private def chunkMultipleOf(chunkMultiple: Long, start: Long, total: Long): ((Long, Long), Long) = {
    val size = total - start
    val chunkSize = (size / chunkMultiple) * chunkMultiple

    val end = start + chunkSize
    val remainder = size - chunkSize

    ((start, end), remainder)
  }
}