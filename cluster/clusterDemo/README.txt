首先，阅读本说明，了解执行顺序.
第一步，进入start文件夹，执行 'mkdir.sh'创建所需的文件夹.
第二步，进入shard文件夹，执行 'shard.sh'，创建 shard server.
第三步，进入config文件夹，执行‘config.sh’，创建 config server.
到这里先等等，确认集群其他机器都完成这三步，然后开始下一步。
第四步，进入mongos文件夹，执行'mongos.sh'，创建 router server.
这一步需要将各分片形成主备，进入各自的node_shard文件夹，执行脚本.同一脚本只需执行一次，谁执行谁就是主。
最后进入startShard文件夹，只需一次，执行'startShard.sh'，启动分片集群 接下来就是神奇的时刻.
如需关闭集群，每台机器执行stop文件夹下的'close.sh'.
下面是设置分片集合与片键的操作：

use admin;首先用mongo连接mongos端口，进入admin数据库
db.runCommand({ listshards:1 });查看集群情况
db.runCommand({ enablesharding:'profileDataStore' });设置分片的数据库
db.runCommand({ shardcollection:'profileDataStore.uav_profile', key:{"appid":1,"time":1}});设定分片集合与片键
db.printShardingStatus();查看分片情况
printShardingStatus(db.getSisterDB("config"),1);同上

use config;进入配置数据库
db.settings.find();查看配置
db.settings.save( { _id:"chunksize", value: 64 } );修改块的大小

use profileDataStore;进入分片数据库
db.uav_profile.getIndexes();查看集合索引
db.uav_profile.createIndex({"_id": 'hashed'});创建索引
db.uav_profile.dropIndex({"_id": 'hashed'});删除索引
db.uav_profile.getShardDistribution();查看数据在集群上的分布


权限控制
MongoDB默认为验证模式。如需对数据库进行权限控制，需先采用无验证模式登录，进入admin库创建管理员用户后，再采用验证模式登录。通过前面创建的管理员帐号进行数据库与用户的创建。
MongoDB集群的权限与单台的权限控制的不同之处在于，单台是通过auth属性，集群是通过keyFile来进行服务器间的验证。
以下介绍配置全过程。前面的所有步骤，都是在nosecurity模式下进行。如果没有采用非验证模式的需要将所有进程（分片、配置、mongos）停止，将切换到无验证模式。

步骤一：先进行登录，并切换进admin库创建管理员帐号

cd $mongoPath
>./mongo $ip:$mongosPort
mongos> use admin
mongos>db.addUser('user','password')

验证用户名与密码
mongos> db.auth('user','password')

mongos>exit

步骤二：退出后，运行stop/close.sh关闭MongoDB的所有进程（分片、配置、mongos），将切换到验证模式。

步骤三，按照无验证模式的运行顺序，重启集群机器全部mongod,mongos进程，运行security文件夹下(shard,config,mongos)的相应脚本。

启动后，如对库进行查看，则会报以下异常：

>./mongo $ip:$mongosPort/admin

> show dbs

Fri Mar 23 22:28:28 uncaughtexception: listDatabases failed:{ "ok" : 0, "errmsg" :"unauthorized" }

以下是正常登录后显示的信息：
>./mongo $ip:$mongosPort/admin

> show dbs

>db.auth('admin','123456')

mongos>

步骤四：以下是数据库及数据库用户创建的过程：

mongos> use hello

switched to db hello

mongos>db.addUser('sa','sa')

mongos> exit

>./mongo $ip:$mongosPort/hello -u sa -p

输入密码，进行登录。

