# 0.9

This version includes a refactoring of the code base and provides some nice new features :-)

Note that, while we are stabilizing the API, some changes may occur between this version and the next in order to provide a more consistent experience.

Here are a quick view of the 0.8⇒0.9 changes:

### Improved commands

…

> RawCommand et les quelques commandes de base, ainsi l'utilisation avec db.command()

> il faut notamment bien expliquer la signature qui n'est pas franchement évidente de prime abord

### BSON library

We made some hehe changes under the ground to provide 2 super nice features: sweet BSONDocument creation (à la Json.obj) and macro ☺

Say you want to create the BSON writer of this case class:

```scala
case class User(
  lastName: String,
  firstNames: Seq[String],
  age: Int,
  createdAt: DateTime
)
```

Here is the classic version:

```scala
// 0.8
implicit object UserWriter extends BSONDocumentWriter[User] {
  def write(user: User) = BSONDocument(
    "firstNames" -> BSONArray(user.firstNames.map { name =>
      BSONString(name)
    }),
    "lastName" -> BSONString(user.lastName),
    "age" -> BSONInteger(user.age),
    "createdAt" -> BSONDateTime(user.createdAt)
  )
}

// 0.9
implicit object UserWriter extends BSONDocumentWriter[User] {
  def write(user: User) = BSONDocument(
    "firstNames" -> user.firstNames,
    "lastName" -> user.lastName,
    "age" -> user.age,
    "createdAt" -> user.createdAt
  )
}
```

And here is the macro version:

```scala
// 0.9 only
implicit val writer = Macro.writer[User]
```


### Collections have been refactored and greatly improved

We changed the global architecture in order to separate the MongoDB's “collection” concept and the API to use them.

It is now possible (and relatively easy) to implement your own Collection, and then create an abstraction layer without requiring an ORM.

#### Smooth integration of the QueryBuilder

The QueryBuilder and the Collection have been merged the resulting new Collection API is far more friendly.

The types of query and result are now specified in different places, allowing of specify only one of them.

Example of a basic query:

```scala
// 0.8
collection.find[BSONDocument, User](BSONDocument("name" -> BSONString("foo")))

// 0.9
collection.find(BSONDocument("name" -> "foo")).cursor[User]
```

The same one, with sorting:

```scala
// 0.8
collection.find[User](QueryBuilder(BSONDocument("name" -> BSONString("foo")).sort("name" -> BSONInteger(1)))

// 0.9
collection.find(BSONDocument("name" -> "foo"))
          .sort(BSONDocument("age" -> 1))
          .cursor[User]
```

The collection returns a QueryBuilder object to allow modifications, then the `cursor` method performs the request and returns a cursor representation (which can be transform into Future or Enumerator).

#### Specialized collections

In a Play! application, it is usual to specify how to import/export the model in JSON format (to communicate with browser for instance), and sometimes the chosen format is exactly the same that the one you would used for database. For instance, if you used `Json.format[User]`.
In such cases, you have to retrieve JsValue from MongoDB, and then to manually use JSON's formatter to instantiate your model.

This job be done automatically?

As usual, you have many options here: stop using JSON format, build a new layer on top of collections, build and ORM… or just specialize the used collection :-)

The `JSONCollection` has been introduced in ReactiveMongo 0.9's Play! plugin. This collection looks exactly like the `DefaultCollection` (the one you use when you don't specify anything) but use JSON's reader and writer instead of BSON's ones.

```scala
case class User(name: String, age: Int)
implicit val formatter = Json.format[User]
coll = db.collection[Json]("users")
coll.find(BSONDocument("name" -> "foo")).cursor[User]
```

This specific collection belongs the the Play! plugin because it is linked to the Play!'s JSON API, but of course this plugin is _not_ mandatory to build your own specialized collection ;-)

### GridFS support

…

This part will be cleaned and simplified in the next version.
