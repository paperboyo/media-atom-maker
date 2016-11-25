package model

import model.Platform.Youtube
import org.cvogt.play.json.Jsonx

import com.gu.contentatom.thrift.{
Atom => ThriftAtom,
AtomType => ThriftAtomType,
AtomData
}

import com.gu.contentatom.thrift.atom.media.{
MediaAtom => ThriftMediaAtom,
Metadata => ThriftMetadata
}

import _root_.util.atom.MediaAtomImplicits

// Note: This is *NOT* structured like the thrift representation
case class MediaAtom(
  // Atom wrapper fields
  id: String,
  labels: List[String],
  contentChangeDetails: ContentChangeDetails,
  // data field
  assets: List[Asset],
  activeVersion: Option[Long],
  title: String,
  category: Category,
  plutoProjectId: Option[String],
  duration: Option[Long],
  source: Option[String],
  description: Option[String],
  posterImage: Option[Image],
  // metadata
  tags: List[String],
  youtubeCategoryId: Option[String],
  license: Option[String],
  channelId: Option[String],
  commentsEnabled: Boolean = false
)  {

  def asThrift = ThriftAtom(
      id = id,
      atomType = ThriftAtomType.Media,
      labels = List(),
      defaultHtml = generateHtml(),
      data = AtomData.Media(ThriftMediaAtom(
        assets = assets.map(_.asThrift),
        activeVersion = activeVersion,
        title = title,
        category = category.asThrift,
        plutoProjectId = plutoProjectId,
        duration = duration,
        source = source,
        posterUrl = posterImage.flatMap(_.master).map(_.file),
        description = description,
        posterImage = posterImage.map(_.asThrift),
        metadata = Some(ThriftMetadata(
          tags = Some(tags),
          categoryId = youtubeCategoryId,
          license = license,
          commentsEnabled = Some(commentsEnabled),
          channelId = channelId
          ))
        )),
      contentChangeDetails = contentChangeDetails.asThrift,
      flags = None
  )

  private def generateHtml(): String = {
    val activeAssets = assets filter (asset => activeVersion.contains(asset.version))
    if (activeAssets.nonEmpty && activeAssets.forall(_.platform == Platform.Url)) {
      views.html.MediaAtom.embedUrlAssets2(posterImage.flatMap(_.master).map(_.file).getOrElse(""), activeAssets).toString
    } else {
      activeAssets.headOption match {
        case Some(activeAsset) if activeAsset.platform == Platform.Youtube =>
          views.html.MediaAtom.embedYoutubeAsset2(activeAsset).toString
        case _ => "<div></div>"
      }
    }
  }
}

object MediaAtom extends MediaAtomImplicits {
  implicit val mediaAtomFormat = Jsonx.formatCaseClass[MediaAtom]

  def fromThrift(atom: ThriftAtom) = {
    val data = atom.tdata

    MediaAtom(
      id = atom.id,
      labels = atom.labels.toList,
      contentChangeDetails = ContentChangeDetails.fromThrift(atom.contentChangeDetails),
      assets = data.assets.map(Asset.fromThrift).toList,
      activeVersion = data.activeVersion,
      title = data.title,
      category = Category.fromThrift(data.category),
      plutoProjectId = data.plutoProjectId,
      duration = data.duration,
      source = data.source,
      posterImage = data.posterImage.map(Image.fromThrift),
      description = data.description,
      tags = data.metadata.flatMap(_.tags.map(_.toList)).getOrElse(Nil),
      youtubeCategoryId = data.metadata.map(_.categoryId).getOrElse(None),
      license = data.metadata.flatMap(_.license),
      commentsEnabled = data.metadata.flatMap(_.commentsEnabled).getOrElse(false),
      channelId = data.metadata.flatMap(_.channelId)
    )
  }
}