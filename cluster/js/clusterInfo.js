print("===============mongos================\n");
var cursor = db.mongos.find({});
while(cursor.hasNext())
{
      var temp = cursor.next();
      printjson(temp._id);  
}
print("===============config================\n");
var cursor = db.serverStatus();
printjson(cursor.sharding);
print("===============shards================\n");
var cursor = db.shards.find({})
while(cursor.hasNext())
{
      var temp = cursor.next();
      printjson(temp);  
}