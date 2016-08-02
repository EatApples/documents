var cursor = db.databases.find({})
while(cursor.hasNext())
{
      var temp = cursor.next();
      printjson(temp);  
}