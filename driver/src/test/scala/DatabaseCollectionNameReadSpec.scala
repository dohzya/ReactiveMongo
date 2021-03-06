import concurrent.Await
import org.specs2.mutable.Specification
import concurrent.duration._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONString
import reactivemongo.bson.{ BSONString, BSONDocument }

class DatabaseCollectionNameReadSpec extends Specification {
  sequential

  import Common._

  "ReactiveMongo db" should {
    val db2 = db.sister("specs2-test-reactivemongo-DatabaseCollectionNameReadSpec")

    "query names of collection from database" in {

      val c1: BSONCollection = db2("collection_one")

      Await.result(c1.insert(BSONDocument("one" -> BSONString("one"))), DurationInt(10) second)

      val c2: BSONCollection = db2("collection_two")

      Await.result(c2.insert(BSONDocument("one" -> BSONString("two"))), DurationInt(10) second)

      Await.result(db2.collectionNames, DurationInt(10) second)
        .mustEqual(Seq("system.indexes", "collection_one", "collection_two"))
    }

    "remove db..." in {
      Await.result(db2.drop, DurationInt(10) second) mustEqual true
    }
  }
}
