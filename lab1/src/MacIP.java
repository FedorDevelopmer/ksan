import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;

public class MacIP {

    public static void main(String[] args) throws Exception {
        Enumeration<NetworkInterface> allNetInt = NetworkInterface.getNetworkInterfaces();
        InetAddress ip =null;
        while(allNetInt.hasMoreElements()){
            NetworkInterface netInt = allNetInt.nextElement();
            Enumeration<InetAddress> addr = netInt.getInetAddresses();
            byte[] mac = netInt.getHardwareAddress();
            while(addr.hasMoreElements()){
                ip = addr.nextElement();
                if(ip!=null)
                {
                    if(ip instanceof Inet4Address){
                        System.out.println("Interface: "+netInt.getDisplayName());
                        System.out.println("Interface IP: "+ip.getHostAddress());
                        if(mac!=null) {
                            StringBuilder sb = new StringBuilder();
                            for (byte b : mac) {
                                sb.append(String.format("%02x-", b));
                            }
                            sb.delete(sb.length()-1,sb.length());
                            System.out.println("Interface MAC: "+sb);
                        }
                        System.out.println();
                        SendRequestIP4(netInt,ip.getAddress(),netInt.getInterfaceAddresses().get(0).getNetworkPrefixLength());
                    }


                }
            }

        }
    }
    public static void SendRequestIP4(NetworkInterface inter,byte[] ip,int MaskLength) throws IOException{
        long mask = SetMask(MaskLength);
        long min=BytesToLong(ip);
        long max=min;
        long addr;
        //forming minimal of addresses with mask(bitwise AND)
        min&=mask;
        //increasing,min address is not using on practice
        addr=min+1;
        //next 3 lines for editing mask to count max,shifts bc mask type is long
        mask=~mask;
        mask<<=32;
        mask>>=32;
        //forming maximal of addresses with mask(bitwise OR)
        max|=mask;
        byte[] curr=LongToBytes(addr,4);
        if(curr[0]!=127){
            while(addr>min&&addr<max){
                System.out.print("Processing: " +(int)(((Math.abs((double)addr-(double)min))/((double)max-(double)min))*100)+"%"+ "\r");
                String ip_address=Byte.toUnsignedInt(curr[0])+"."+Byte.toUnsignedInt(curr[1])+"."+Byte.toUnsignedInt(curr[2])+"."+Byte.toUnsignedInt(curr[3]);
                if(InetAddress.getByAddress(curr).isReachable(1500)) {
                    //String ip_adress=Byte.toUnsignedInt(curr[0])+"."+Byte.toUnsignedInt(curr[1])+"."+Byte.toUnsignedInt(curr[2])+"."+Byte.toUnsignedInt(curr[3]);
                    System.out.println(" Name:"+InetAddress.getByAddress(curr).getCanonicalHostName());
                    if(addr!=BytesToLong(ip)){
                        ShowMAC(ip_address);
                    }
                    else{
                        byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getByName(ip_address)).getHardwareAddress();
                        StringBuilder sb = new StringBuilder();
                        for (byte b : mac) {
                            sb.append(String.format("%02x-", b));
                        }
                        sb.delete(sb.length()-1,sb.length());
                        System.out.println(sb);
                        System.out.println();
                    }


                }
                addr++;
                curr=LongToBytes(addr,4);
            }
        }
    }
    public static long BytesToLong(byte[] arr){
        long result=0;
        for(int i=0;i<arr.length;i++){
            result<<=8;
            result+=(Byte.toUnsignedInt(arr[i]));
        }
        return result;
    }
    public static byte[] LongToBytes(long num,int length){
        byte[] arr = new byte[length];
        for(int i=0;i<arr.length;i++){
            arr[length-i-1]=(byte)(num%(long)(Math.pow(2,8)));
            num>>=8;
        }
        return arr;
    }
    public static long SetMask(int length){
        long res=0;
        for(int i=0;i<32;i++){
            res<<=1;
            if(i<length){
                res++;
            }

        }
        return res;
    }
    public static void ShowMAC(String ip_address) throws IOException{
        //using arp command ato et MAC-adr as MAC could be get only on low-level commands
        Process pr = Runtime.getRuntime().exec("arp "+ip_address+" -a");
        BufferedReader Input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String s = Input.readLine();
        for(int i=0;i<3;i++){
            s=Input.readLine();
        }
        if(s!=null){
            s=s.trim();
            s=s.substring(s.indexOf(" "));
            s=s.trim();
            s=s.substring(0,s.indexOf(" "));

            System.out.println(" MAC: "+s);
            System.out.println();
        }

        pr.destroy();
    }
}


