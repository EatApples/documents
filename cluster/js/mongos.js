var cursor = db.mongos.find({});
while(cursor.hasNext())
{
      var temp = cursor.next();
      printjson(temp);  
}