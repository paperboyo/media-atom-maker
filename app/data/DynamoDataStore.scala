package data

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.scanamo.error.{ DynamoReadError, TypeCoercionError }
import javax.inject.Inject
import com.gu.contentatom.thrift.Atom
import com.gu.scanamo.{ Scanamo, DynamoFormat, Table }
import com.gu.scanamo.query._
import com.twitter.scrooge.CompactThriftSerializer
import cats.data.Xor

import model.ThriftUtil._

class DynamoDataStore(dynamo: AmazonDynamoDBClient, tableName: String) extends DataStore {
  @Inject() def this(aws: util.AWS) = this(aws.dynamoDB, aws.dynamoTableName)

  case class AtomRow(
    id: String,
    version: Long,
    atom: String // atom data serialized by thrift
  )

  object AtomRow {
    def apply(atom: Atom): AtomRow = AtomRow(
      atom.id,
      atom.mediaData.activeVersion,
      atomSerializer.toString(atom)
    )
  }

  // you can't use BinaryThriftStructSerializer from crooge-serializer
  // if Thrift is greater than 0.9.0 as they use a method that has
  // been removed (see https://github.com/twitter/scrooge/issues/203)
  // Also the JsonThriftserializer is one way only (it doesn't store
  // the Thrift type data because it uses the TSimpleJsonProtocol)

  private val atomSerializer = CompactThriftSerializer(Atom)

  private def rowToAtom(row: AtomRow): Xor[DynamoReadError, Atom] = try {
    Xor.Right(atomSerializer.fromString(row.atom))
  } catch {
    case err: org.apache.thrift.TException => Xor.Left(TypeCoercionError(err))
  }

  implicit val dynamoFormat: DynamoFormat[Atom] =
    DynamoFormat.xmap(rowToAtom _)(AtomRow.apply _)(DynamoFormat[AtomRow]) // <- just saving a new implicit here

  // useful shortcuts
  private val get = Scanamo.get[Atom](dynamo)(tableName) _
  private val put = Scanamo.put[Atom](dynamo)(tableName) _

  val atomsTbl = Table[Atom](tableName)

  // this should probably return an Either so we can report an error,
  // e.g. if the atom exists, but it can't be deseralised
  def getMediaAtom(id: String): Option[Atom] = get(UniqueKey(KeyEquals('id, id))) match {
    case Some(Xor.Right(atom)) => Some(atom)
    case _ => None
  }

  def createMediaAtom(atom: Atom): Unit =
    if(get(UniqueKey(KeyEquals('id, atom.id))).isDefined)
      throw IDConflictError
    else
      put(atom)

  def updateMediaAtom(newAtom: Atom): Unit = ??? //getMediaAtom

}
