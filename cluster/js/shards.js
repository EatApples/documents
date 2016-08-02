var cursor = db.shards.find({})
while(cursor.hasNext())
{
      var temp = cursor.next();
      printjson(temp);  
}