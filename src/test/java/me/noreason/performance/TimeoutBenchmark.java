package me.noreason.performance; /**
 * Created on 2014-11-11
 * <p/>
 * author: MSK
 * description:
 */
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.*;

class TraceId {
    private static ThreadLocal<String> value = new ThreadLocal<String>();

    public static String getId(){
        String v = value.get();
        return v == null ? "-": v;
    }

    public static void setId(String id){
        value.set(id);
    }
}

class logger {
    static Logger logger = Logger.getLogger("timeout");
    static {
        logger.setLevel(Level.INFO);
        FileHandler handler = null;
        try {
            handler = new FileHandler("./result.log");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        MyFormater fmt = new MyFormater();
        assert handler != null;
        handler.setFormatter(fmt);
        logger.addHandler(handler);
    }
    public static void info(String message){
        logger.info(message);
    }
    public static void error(String message){
        logger.severe(message);
    }
}

class MyFormater extends SimpleFormatter {
    public static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
    @Override
    public String format(LogRecord record) {
        //%(asctime)s %(threadName)s %(levelname)s %(name)s [%(trace_sn)s] %(message)s
        String time = fmt.format(new Date());
        String threadName = Thread.currentThread().getName();
        String level = record.getLevel().toString();
        String name = record.getLoggerName();
        String trace_sn = TraceId.getId();
        String message = record.getMessage();
        return String.format("%s %s %s %s [%s] %s\r\n", time,threadName, level, name,trace_sn, message);
    }
}

class SocketClient{
    private String host;
    private int port;
    private int timeout = 30;
    public SocketClient(String host, int port, int timeout){
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    public SocketClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    public String send_packet(String reqPacket){
        Socket socket = null;
        String response = null;
        try {
            //创建一个流套接字并将其连接到指定主机上的指定端口号
            logger.info(String.format("connecting %s:%d", this.host, this.port));
            socket = new Socket(this.host, this.port);
            socket.setSoTimeout(this.timeout * 1000);
            //读取服务器端数据
            logger.info(String.format("send to server [%s:%d] packet:%s", this.host, this.port, reqPacket));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.write(reqPacket.getBytes("GBK"));
            out.flush();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] buffer = new byte[1024];
            int len = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            logger.info("recving data");
            do{
                len = in.read(buffer);
                logger.info(String.format("read %d data", len));
                if (len > 0)
                    baos.write(buffer,0,len);
            } while (len > 0);
            byte[] resultBuffer = baos.toByteArray();
            if (resultBuffer.length == 0){
                logger.error("从服务器获取数据为空");
                throw new RuntimeException("empty data");
            }
            response = new String(resultBuffer);
            logger.info("recv packet:" + response);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response;
    }
}
public class TimeoutBenchmark {

    public static class RequestWorker extends Thread{

        private int no;
        private int times;
        private int seq = 0;
        private static String template = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<Message><Message_Header><bank_no>3132410666612410</bank_no><channel_id>C1001001</channel_id><status>4635613FB1965F42</status><transaction_sn>%s</transaction_sn><request_time>20140829144204</request_time><response_time>20140829144204</response_time><service_id>MB2001</service_id><version_id>1.0</version_id><response_code></response_code><response_info></response_info></Message_Header><Message_Body><request><account>130CF71E51A232FD309C2C3BBEC76BF4E055EA1E9589AA85</account><currency>156</currency><pagesize>10</pagesize><pagenumber>1</pagenumber></request><response/></Message_Body></Message>\r\n\r\n";

        public RequestWorker(int no, int times){
            this.no = no;
            this.times = times;
        }

        public void run(){
            do{
                this.times--;
                this.work();
            }while (this.times > 0);
        }

        public void work(){
            try {
                String id = this.nextId();
                TraceId.setId(id);
                SocketClient client = new SocketClient("10.172.81.51", 21011);
                client.send_packet(String.format(template, id));
            }catch (Exception e){
                System.err.println(TraceId.getId());
                e.printStackTrace();
            }

        }

        public String nextId(){
            this.seq++;
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
            Date now = new Date();
            return String.format("%s%02d%04d", fmt.format(now), this.no, this.seq);
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int worker_num = 1;
        int times = 1;
        if (args.length == 2){
            worker_num = Integer.parseInt(args[0]);
            times = Integer.parseInt(args[1]);
        }
        logger.info(String.format("start benchmark worker_num:%d times:%d" , worker_num, times));
        List<RequestWorker> workers = new ArrayList<RequestWorker>(worker_num);
        for (int i=0; i< worker_num; i++){
            workers.add(new RequestWorker(i, times));
        }
        for (RequestWorker worker: workers) worker.start();
        for (RequestWorker worker: workers) worker.join();
        logger.info("done");
    }
}
