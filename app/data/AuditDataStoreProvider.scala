package data

import ai.x.diff.DiffShow
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import javax.inject.{ Inject, Provider }
import util.AWSConfig
import org.joda.time.DateTime
import model.{MediaAtom, Audit}
import scala.collection.JavaConversions._

class AuditDataStoreProvider @Inject() (awsConfig: AWSConfig)
    extends Provider[AuditDataStore] {
  def get = new AuditDataStore(awsConfig.dynamoDB, awsConfig.auditDynamoTableName)
}

class AuditDataStore(client: AmazonDynamoDBClient, auditDynamoTableName: String) {
  lazy val db = new DynamoDB(client).getTable(auditDynamoTableName)

  def getAuditTrailForAtomId(id: String): List[Audit] = {
    db.query("atomId", id).map(Audit.fromItem).toList
  }

  def auditPublish(atomId: String, username: Option[String]) = {
    val audit = Audit(atomId, "publish", None, DateTime.now().getMillis, username.getOrElse("unknown user"))
    db.putItem(audit.toItem)
  }

  def auditCreate(atomId: String, username: Option[String]) = {
    val audit = Audit(atomId, "create", None, DateTime.now().getMillis, username.getOrElse("unknown user"))
    db.putItem(audit.toItem)
  }

  def auditUpdate(atomId: String, username: Option[String], description: String) = {
    val audit = Audit(atomId, "update", Some(description), DateTime.now().getMillis, username.getOrElse("unknown user"))
    db.putItem(audit.toItem)
  }

  private val interestingFields = List("title", "category", "description", "duration", "source", "youtubeCategoryId", "license", "commentsEnabled", "channelId", "legallySensitive")

  // We don't use HTTP patch so diffing has to be done manually
  def createDiffString(before: MediaAtom, after: MediaAtom): String = {
    val fieldDiffs = DiffShow.diff[MediaAtom](before, after).string
      .replaceAll("\\[*[0-9]+m", "") // Clean out the silly console coloring stuff
      .split('\n')
      .map(_.trim())
      .filter(line => !line.contains("ERROR")) // More silly stuff from diffing library
      .filter(line => interestingFields.exists(line.contains))
      .mkString(", ")

    if (fieldDiffs == "") { // There's a change, but in some field we're not interested in (or rather, unable to format nicely)
      "Updated atom fields"
    } else {
      s"Updated atom fields ($fieldDiffs)"
    }
  }
}