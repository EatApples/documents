var cursor = db.settings.find({})
while(cursor.hasNext())
{
      var temp = cursor.next();
      printjson(temp);  
}