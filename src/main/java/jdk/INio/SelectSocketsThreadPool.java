package jdk.INio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 使用线程池为通道提供服务
 *
 * @author Jian Shen
 * @version V1.0
 * @date 2018/11/14
 */
public class SelectSocketsThreadPool extends SelectSockets {

    private static final int THREAD_POOL_SIZE = 5;
    private ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) throws IOException {
        new SelectSocketsThreadPool().go();
    }

    @Override
    protected void readDataFromChannel(SelectionKey selectionKey) {
        WorkerRunnable workerRunnable = new WorkerRunnable();
        workerRunnable.serviceChannel(selectionKey);
        executorService.submit(workerRunnable);
    }

    private class WorkerRunnable implements Runnable {

        private SelectionKey selectionKey;
        private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        @Override
        public synchronized void run() {
            System.out.println(Thread.currentThread().getName() + "is ready....");
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (selectionKey == null) {
                return ;
            }

            try {
                drainChannel(selectionKey);
            } catch (IOException e) {
                e.printStackTrace();
                selectionKey.selector().wakeup();
            }
            selectionKey = null;
        }

        void drainChannel(SelectionKey selectionKey) throws IOException {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            byteBuffer.clear();
            while(socketChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                // 仅作示例，该操作又写入socketChannel，请根据实际用处进行编写
                while (byteBuffer.hasRemaining()) {
                    socketChannel.write(byteBuffer);
                }
                byteBuffer.clear();
            }
            socketChannel.close();
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_READ);
            selectionKey.selector().wakeup();
        }

        synchronized void serviceChannel(SelectionKey selectionKey) {
            this.selectionKey = selectionKey;
            selectionKey.interestOps(selectionKey.interestOps() & (~SelectionKey.OP_READ));
            this.notify();
        }
    }
}
