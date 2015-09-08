package me.noreason.performance.webservice;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
            e.printStackTrace();
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

class WebServiceClient {
    private String host;
    private int port;
    private int timeout = 30;

    public WebServiceClient(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    public WebServiceClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String send_packet(String url, String data) throws IOException {
        //创建一个流套接字并将其连接到指定主机上的指定端口号
        logger.info(String.format("connecting %s:%d", this.host, this.port));
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setEntity(EntityBuilder.create().setText(data).build());
        //读取服务器端数据
        logger.info(String.format("send to server [%s:%d]", this.host, this.port));
        HttpResponse httpResponse = client.execute(post);
        logger.info("response code" + httpResponse.getStatusLine());
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            logger.error("repsonse error");
            throw new RuntimeException("response error");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        httpResponse.getEntity().writeTo(baos);
        String response = new String(baos.toByteArray());
        if (!response.contains("<response_code>00000000</response_code>")) {
            logger.error("response data error");
            throw new RuntimeException("response data error");
        }
        return response;
    }
}

public class BC001Benchmark {

    public static class RequestWorker extends Thread{

        private int no;
        private int times;
        private int seq = 0;
        private static String data = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:targetNs=\"urn:UserDetailsByMobileNumber\" xmlns:ns1=\"http://message.server.webservices.jilin.projects.wsc.csam.com\"><soap:Header></soap:Header><soap:Body><targetNs:fetchUserDetailsByMobileNumber><in0><messageHeader><transaction_sn>EFS2030091588992807</transaction_sn><service_id>BC_001</service_id><bank_no>019801</bank_no><channel_id></channel_id><request_time>20300915111022</request_time><response_time></response_time><version_id></version_id><status>D0EC7A281264931F</status><response_code></response_code><response_info></response_info></messageHeader><messageBody><request><mobileNumber></mobileNumber><idType>A</idType><idNumber>6B250B4CC44EF9E56B786186F9E3E93696D6F2E36AC3F31DE2575713E9B0869324A9E093B6DE5796</idNumber><userAlias></userAlias><password>E9E6C35D0BEA1951</password><tellerId>000028</tellerId><verifiedAccountNumber>6228650400020150153</verifiedAccountNumber></request></messageBody></in0></targetNs:fetchUserDetailsByMobileNumber></soap:Body></soap:Envelope>";

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
                WebServiceClient client = new WebServiceClient("192.168.2.161", 8001);
                client.send_packet("/MBSI/Proxy/ws/UserDetailsByMobileNumber", data);
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
