import com.google.inject.AbstractModule
import com.gu.atom.data._
import com.gu.atom.publish._
import com.gu.pandomainauth.action.AuthActions
import data._
import com.gu.pandahmac.HMACAuthActions

class Module extends AbstractModule {
  def configure() = {
    bind(classOf[HMACAuthActions])
      .to(classOf[controllers.PanDomainAuthActions])

    bind(classOf[PublishedDataStore])
    .toProvider(classOf[PublishedMediaAtomDataStoreProvider])

    bind(classOf[PreviewDataStore])
      .toProvider(classOf[PreviewMediaAtomDataStoreProvider])

    bind(classOf[LiveAtomPublisher])
    .toProvider(classOf[LiveAtomPublisherProvider])

    bind(classOf[PreviewAtomPublisher])
      .toProvider(classOf[PreviewAtomPublisherProvider])

    bind(classOf[PreviewAtomReindexer])
      .toProvider(classOf[PreviewAtomReindexerProvider])

    bind(classOf[PublishedAtomReindexer])
      .toProvider(classOf[PublishedAtomReindexerProvider])
  }
}
