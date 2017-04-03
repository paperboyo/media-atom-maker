package util

import com.gu.media.aws.SESSettings
import com.gu.media.ses.Mailer
import com.gu.media.upload.actions.{UploaderAccess, UploadActionHandler}
import com.gu.media.youtube.{YouTube, YouTubeUploader}
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.AddAssetCommand

/**
  * For use in dev, uploads the video and adds the asset directly rather than via a Lambda
  */
class DevUploadHandler(stores: DataStores, access: UploaderAccess, youTube: YouTube, mailer: Mailer)
  extends UploadActionHandler(stores.uploadStore, stores.pluto, access, new YouTubeUploader(access, youTube), mailer) {

  private val user = PandaUser("Media", "Atom Maker", "media-atom-maker@theguardian.co.uk", None)

  override def addAsset(atomId: String, videoId: String): Long = {
    val videoUri = s"https://www.youtube.com/watch?v=$videoId"
    val atom = AddAssetCommand(atomId, videoUri, stores, youTube, user).process()
    val versions = atom.assets.map(_.version).sorted

    versions.last
  }
}