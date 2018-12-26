package org.apache.zookeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws Exception {
        String connectUrl = "127.0.0.1:2181";
        int sessionTimeout = 1000 * 60;
        CountDownLatch connectedLatch = new CountDownLatch(1);
        DefaultWatcher watcher = new DefaultWatcher(connectedLatch);
        ZooKeeper zooKeeper = new ZooKeeper(connectUrl, sessionTimeout, watcher);

        if (ZooKeeper.States.CONNECTING == zooKeeper.getState()) {

            boolean ret = connectedLatch.await(sessionTimeout, TimeUnit.MILLISECONDS);

            // 如果等待超时了，还没有收到连接成功的通知，则说明zk不可用，直接不用zk,并报警
            if (!ret) {
                System.out.println("超时");
            }
        }
        System.out.println(zooKeeper);
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
