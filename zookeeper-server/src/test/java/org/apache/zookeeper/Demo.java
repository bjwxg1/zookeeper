package org.apache.zookeeper;

import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws Exception {
        String connectUrl = "127.0.0.1:2181";
        String path = "/demoRoot";
        int sessionTimeout = 1000 * 60;
        Stat stat =new Stat();
        CountDownLatch connectedLatch = new CountDownLatch(1);
        DefaultWatcher watcher = new DefaultWatcher(connectedLatch);

        ZooKeeper zooKeeper = new ZooKeeper(connectUrl, sessionTimeout, watcher);
        DataProcessWatcher dataWatcher = new DataProcessWatcher(zooKeeper,path);


        if (ZooKeeper.States.CONNECTING == zooKeeper.getState()) {
            boolean ret = connectedLatch.await(sessionTimeout, TimeUnit.MILLISECONDS);
            // 如果等待超时了，还没有收到连接成功的通知，则说明zk不可用，直接不用zk,并报警
            if (!ret) {
                System.out.println("超时");
            }
        }

        //zooKeeper.create(path,"hello".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL,stat);
        System.out.println(stat);
        byte[] result = zooKeeper.getData(path,watcher,stat);
        System.out.println(new String(result));
        Thread.currentThread().sleep(1000*60*60);
    }

}

   class DefaultWatcher implements Watcher {

    private static final String MODULE_NAME = "[DefaultWatcher]";
    private CountDownLatch connectedLatch;

    public DefaultWatcher(CountDownLatch connectedLatch) {
        this.connectedLatch = connectedLatch;
    }

    // 监控所有被触发的事件
    @Override
    public void process(WatchedEvent event) {
        if (connectedLatch != null && event.getState() == Event.KeeperState.SyncConnected) {
            connectedLatch.countDown();
        }
    }



}
class DataProcessWatcher implements Watcher{
    private static final String MODULE_NAME = "[DefaultWatcher]";
    private ZooKeeper zk;
    private String path;

    public DataProcessWatcher(ZooKeeper zk, String path) {
        this.zk = zk;
        this.path = path;
    }

    // 监控所有被触发的事件
    @Override
    public void process(WatchedEvent event) {
        try{
            if ( event.getState() == Event.KeeperState.SyncConnected) {
                if(Event.EventType.NodeDataChanged == event.getType()){
                    byte[] result = this.zk.getData(this.path,new DataProcessWatcher(this.zk,this.path),new Stat());
                    System.out.println(new String(result));
                }
            }
        }catch (Exception e){

        }
    }
}

