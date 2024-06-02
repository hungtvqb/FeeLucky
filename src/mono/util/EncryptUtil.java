/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.util;

import mono.GlobalConfig;
import mono.cache.HCache;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.util.encoders.Base64;

public class EncryptUtil {

    private static final Logger log = Logger.getLogger(EncryptUtil.class);

    private static String AESKeyGen() throws NoSuchAlgorithmException {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128, new SecureRandom());
            SecretKey secretKey = keyGen.generateKey();
            return byteToHex(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException noSuchAlgo) {
            log.error(noSuchAlgo.getMessage());
        }
        return null;
    }

    private static String byteToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    private static byte[] hexToBytes(char[] hex) {
        int length = hex.length / 2;
        byte[] raw = new byte[length];
        for (int i = 0; i < length; i++) {
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            int value = (high << 4) | low;
            if (value > 127) {
                value -= 256;
            }
            raw[i] = (byte) value;
        }
        return raw;
    }

    private static byte[] hexToBytes(String hex) {
        return hexToBytes(hex.toCharArray());
    }

    private static String encryptAES(String data, String key) throws Exception {
        String dataEncrypted = new String();
        try {
            Cipher aesCipher = Cipher.getInstance("AES");
            byte[] raw = hexToBytes(key);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] byteDataToEncrypt = data.getBytes();
            byte[] byteCipherText = aesCipher.doFinal(byteDataToEncrypt);
            dataEncrypted = DatatypeConverter.printBase64Binary(byteCipherText);
            return dataEncrypted;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException ex) {
            log.error(ex.getMessage());
        }
        return dataEncrypted;
    }

    private static RSAKeyParameters getBCPublicKeyFromString(String strPublicKey) {
        try {
            PublicKey prvKey = getPublicKeyFromString(strPublicKey);
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec pkSpec = keyFac.getKeySpec(prvKey, RSAPublicKeySpec.class);
            RSAKeyParameters pub = new RSAKeyParameters(false, pkSpec.getModulus(), pkSpec.getPublicExponent());
            return pub;
        } catch (Exception e) {
            log.info("Exception : " + e);
            return null;
        }
    }

    private static PublicKey getPublicKeyFromString(String key) throws Exception {
        PublicKey publicKey = null;
        try {
            try (PEMReader reader = new PEMReader(new StringReader(key), null, "SunRsaSign")) {
                publicKey = (PublicKey) reader.readObject();
            }

        } catch (Exception e) {
            log.error("error getPublicKeyFromString ", e);
        }
        return publicKey;
    }

    private static String encryptRSA(String toEncrypt, String strPublicKey) {
        RSAKeyParameters rsaPbKey = getBCPublicKeyFromString(strPublicKey);
        if (rsaPbKey == null) {
            log.info("RSAPublicKey == null");
            return null;
        }
        try {
            AsymmetricBlockCipher theEngine = new RSAEngine();
            theEngine = new PKCS1Encoding(theEngine);
            theEngine.init(true, rsaPbKey);
            return new String(Base64.encode(theEngine.processBlock(toEncrypt.getBytes(), 0, toEncrypt.getBytes().length)));
        } catch (InvalidCipherTextException ex) {
            log.error("encryptRSA error ", ex);
        }
        return null;
    }

    private static String createMsgSignature(String data, String strPrivateKey) {
        String encryptData = "";
        try {
            PrivateKey privateKey = getPrivateKeyFromString(strPrivateKey);
            java.security.Signature s = java.security.Signature.getInstance("SHA1withRSA");
            s.initSign(privateKey);
            s.update(data.getBytes());
            byte[] signature = s.sign();
            encryptData = new String(Base64.encode(signature));                             //Encrypt data
        } catch (Exception e) {
            log.error("createMsgSignature", e);
        }
        return encryptData;
    }

    private static PrivateKey getPrivateKeyFromString(String key) throws Exception {
        PrivateKey privateKey = null;
        try {
            KeyPair pemPair;
            try (PEMReader reader = new PEMReader(new StringReader(key), null, "SunRsaSign")) {
                pemPair = (KeyPair) reader.readObject();
            }

            privateKey = (PrivateKey) pemPair.getPrivate();
        } catch (Exception e) {
            log.info("error getPrivateKeyFromString " + e);
        }
        return privateKey;

    }

    private static boolean verifyMsgSignature(String encodeText, String strPublicKey, String input) {

        try {
            PublicKey publicKey = getPublicKeyFromString(strPublicKey);
            byte[] base64Bytes = Base64.decode(encodeText);                                 // decode base64
            java.security.Signature sig = java.security.Signature.getInstance("SHA1WithRSA");
            sig.initVerify(publicKey);
            sig.update(input.getBytes());

            return sig.verify(base64Bytes);
        } catch (Exception e) {
            log.error("verifyMsgSignature", e);
        }
        return false;
    }
    
    private static final HCache<String, String> KEY_MPS = new HCache<String, String>("KEY_MPS", 3, HCache.NO_MAX_CAPACITY, HCache.NO_TIMEOUT);
    
    private static String getKeyFile(String filePath) {
        String value = KEY_MPS.get(filePath);
        if (value != null) {
            return value;
        }
        
        File file = new File(filePath);
        StringBuilder contents = new StringBuilder();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            while ((text = reader.readLine()) != null) {
                contents.append(text)
                        .append(System.getProperty(
                                        "line.separator"));
            }
        } catch (IOException e) {
            log.error("getKeyFile", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error("getKeyFile", e);
            }
            
            value = contents.toString();
            if (!value.isEmpty()) {
                KEY_MPS.put(filePath, value);
            }
        }

        return value;
    }

    private static String decryptRSA(String toDecrypt, String strPrivateKey) {

        RSAPrivateCrtKeyParameters rsaPrKey = getBCPrivateKeyFromString(strPrivateKey);
        if (rsaPrKey == null) {
            log.info("RSAPrivateKey == null");
            return null;
        }

        try {
            AsymmetricBlockCipher theEngine = new RSAEngine();
            theEngine = new PKCS1Encoding(theEngine);
            theEngine.init(false, rsaPrKey);
            return new String(theEngine.processBlock(Base64.decode(toDecrypt), 0, Base64.decode(toDecrypt).length));
        } catch (Exception ex) {
            log.error("decryptRSA", ex);
        }
        return null;
    }

    private static RSAPrivateCrtKeyParameters getBCPrivateKeyFromString(String strPrivateKey) {
        try {
            PrivateKey prvKey = getPrivateKeyFromString(strPrivateKey);
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            RSAPrivateCrtKeySpec pkSpec = keyFac.getKeySpec(prvKey, RSAPrivateCrtKeySpec.class);
            RSAPrivateCrtKeyParameters priv = new RSAPrivateCrtKeyParameters(pkSpec.getModulus(),
                    pkSpec.getPublicExponent(), pkSpec.getPrivateExponent(), pkSpec.getPrimeP(),
                    pkSpec.getPrimeQ(), pkSpec.getPrimeExponentP(), pkSpec.getPrimeExponentQ(),
                    pkSpec.getCrtCoefficient());
            return priv;
        } catch (Exception e) {
            return null;
        }
    }

    private static String decryptAES(String dataEncrypt, String key) throws Exception {
        String dataDecrypted = new String();
        try {
            Cipher aesCipher = Cipher.getInstance("AES");
            byte[] raw = hexToBytes(key);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            byte[] decordedValue = DatatypeConverter.parseBase64Binary(dataEncrypt);
            aesCipher.init(Cipher.DECRYPT_MODE, skeySpec, aesCipher.getParameters());
            byte[] byteDecryptedText = aesCipher.doFinal(decordedValue);
            dataDecrypted = new String(byteDecryptedText);
            return dataDecrypted;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | InvalidAlgorithmParameterException |
                IllegalBlockSizeException | BadPaddingException ex) {
            log.error(ex.getMessage());
        }
        return dataDecrypted;
    }

    
    private static String decodeMpsResponse(BufferedReader br, String SER, String SUB) throws Exception {
//       String dir = GlobalConfig.get("mps_key");
       String dir = GlobalConfig.get("mps_key") + SUB + "/";
       String publicKeyViettel = getKeyFile(dir + "PublicKeyVT.pem");
       String privateKeyCP = getKeyFile(dir + "PrivateKeyCP.pem");

       String strTemp = "";
       log.info("================>>>DECODE DATA RETURN FROM MPS<<===============");
       while (null != (strTemp = br.readLine())) {
           if (strTemp.length() > 200) {
               String[] s = strTemp.split("&");
               String dataN = s[0].substring(5);
               String sigN = s[1].substring(4);
               log.info("step add: sigN = " + sigN);
               boolean verify_return = verifyMsgSignature(URLDecoder.decode(sigN), publicKeyViettel, dataN);
               log.info("  step 7: data receive from mps dataN = -----> " + dataN);
               log.info("  step 8: check signuature ---------> " + verify_return);
               String data_decrypt_RSAN = decryptRSA(dataN, privateKeyCP);
               String value_return = data_decrypt_RSAN.split("&")[0].substring(6);
               String key_return = data_decrypt_RSAN.split("&")[1].substring(4);
               String data_complete = decryptAES(value_return, key_return);
               return data_complete;
           } else {
               log.error(strTemp);
           }
       }
       return "";
   }
    
     private static String analyseCodeReturn(String input) {
        log.info("   step 9: decode data return from mps ----> " + input);
        String value = "1|unknown" ;
        String[] a = input.split("&");
        for (String a1 : a) {
            if (a1.split("=")[0].equals("RES")) {
                String key = a1.split("=")[1];
                log.info("   step 10: analyse RES code return ---> " + a1 + "==>  "
                        + StaticCode.getdiscription(key));
                value = key + "|" + StaticCode.getdiscription(key);
                break;
            }
        }
        return value;
    }
    
    public static String sendRequestToMps(String PRO, String SER, String sub_service
            , String mobile, 
            String cmd, String amount,String description, 
            String REQ, String SESS, String cate,String item,Map<String,String> properties)
            throws Exception {
        
//        String sub_service = GlobalConfig.get("mps.sub_service");
//        String SER = GlobalConfig.get("mps.service");
//        String PRO = GlobalConfig.get("mps.provider");
        String url_request_spamsms = makeMpsRequestUrl(sub_service,
                REQ, SESS, "", description, amount,
                mobile, "CLIENT", cmd, PRO, SER, cate,item,properties);

        URL urlre_spamsms = new URL(url_request_spamsms);
        InputStream ips =  urlre_spamsms.openStream();
//        String retuns = "DATA=MV4KY+8S7cWuhhDfu4HsBc/2v+L7h5WBjrH7yQYITL84kK3P3Mq6TXjPkjK9oKkvB30LRmui+FC+3LJbIzbtlpOmRIIoXL2e7f5Mfq/eHSSyhGzutylEflMAEIJqZNKbKRqAFPmkSW+6t/cZu3tT/NHm0AyVawAGQYun9sHBhwQjvuZgiyqBzRwPpoAMJaDSonnhIBgoBbdoe9b3x+bFPs+gHflywQ3RiZ+DQeFIJkZbl+Y5oeEHxDuVWUtOLbr8d+QGjTF3mezbIRZhAuD4Ntsv9VKM2lQcNnfhoa63fLqTvGCctYHcSry6Rlvj+K1834GqmcZ6uoVroxvdHQWgcVEvo0MXmG1OPiLEpLq6OCRvqTidM55dsDlulOUpdi98F3IMboUET36Nl5sVJNrxzc+mkXuz0V8+FFqYRfJLSS0tAqQfjkFEXuop2UnjaOSSSwvdArvZXXbFgA5YnDCgwX3o4oKhY6Xwo+jA62ecZiknCl9hniiCmw20FLa+i/UcXqMXxTL92ci3EYBlCyi+xpkBt6wqj9+AL+pdjz30UfWGcZDGtwYISb3w6lXJQgYVygch3GwfGjSQUsQUsA191gTtPk81tRIGCRwZatxgU4eNGRBUmH/qFrreGWYvqKqQgf0EcgY3+NsYPLWz8Ld9PNYPrsI/wY/e+wE7K0HLt3g=&SIG=LaKIFJUYWV6TgC2En1dN25hgAC0AiAl1oDoQ8hg6NYphtUu2r6G2Wd9d%2FlQIYGvO7iYc2%2BGMmPBwjslc%2BUE157SFCtORLIXJUwFoCq0Svycxj7QKX%2B5dh0Bt5uu6GvGip7Kxh14khnBT9CVUKT2J9w%2BFsimPveOEHP3hW9R84vI51zeulGTXTdeZKLu3caGUGNcel6IRTkNMj2eEM%2FGFRwgbV2ZFPscFpTnXyrLtErN8N%2BrGbb6OYvEtI7%2BMTfLewQGD%2BUfAl%2FzACoeePqFDnSscWD5Bb1RbqFEb871mGiqPj%2FnCiHmjx%2Fk9CzoUoC2nw0XJwI2YhC832bMFdDWT%2FtgFMHRiSNjvpwXo1aqLjorrFYsOKrhWDr0h1dvCs4akD8s%2FKkT7LATBU2piqXxcL%2Fi3eg65qTiPzxsWjdK4oY3Q5w5j4YpZM6WaDwQFGYXMldBQpablvnIFyJI55DXxlzaWruGDJw7%2B41XtmkTRSqECw%2BYYqUrkTwqBsk3p0P2Rb5f77FbBymLFlmXJVJ9bLEreudMdA%2F56CSQrCeAdRYojQxRi7sMrcwwlBVUW2Z%2Fngrb3Zw74comTaYb%2B3k1x%2F5ZHvqKd4pUDbqH3A8gCU0OzwPDSwxg66W15POp0Qqpj7UeA2NKl9waOwhrXChlJTHq%2F%2FMnu%2Bx%2BtJXij1I2aEIs%3D";
//        retuns.
        log.info(" step 6: url request toi SMS GLOBAL API -----> " + urlre_spamsms);
        BufferedReader br_spamsms = new BufferedReader(new InputStreamReader(ips));
        String data_complete_spamsms = decodeMpsResponse(br_spamsms, SER, sub_service);
        return analyseCodeReturn(data_complete_spamsms);
    }

    private static String makeMpsRequestUrl(String SUB, String REQ,
            String SESS, String SUB_CP, String CONT, String PRICE, String MOBILE, String source,
            String CMD, String PRO, String SER,String cate,String item, Map<String,String> properties) 
            throws NoSuchAlgorithmException, Exception {

        String url_wap_mobile = GlobalConfig.get("mps_url");
        String dir = GlobalConfig.get("mps_key") + SUB + "/";
        log.info("======> check key at folder :" + dir);
        String publicKeyViettel = getKeyFile(dir + "PublicKeyVT.pem");
        String privateKeyCP = getKeyFile(dir + "PrivateKeyCP.pem");
        String publicKeyCP = getKeyFile(dir + "PublicKeyCP.pem");
        String keyAES = AESKeyGen();
        StringBuffer input = new StringBuffer();
        input.append("SUB=").append(SUB)
                .append("&REQ=").append(REQ)
                .append("&SESS=").append(SESS)
                .append("&SUB_CP=").append(SUB_CP)
                .append("&CONT=").append(CONT)
                .append("&PRICE=").append(PRICE)
                .append("&MOBILE=").append(MOBILE)
                .append("&ITEM=").append(item)
                .append("&SOURCE=").append(source)
                .append("&TYPE=MOBILE")
                .append("&IMEI=NULL")
//                 .append("&CATE=").append(cate)
                ;
        if (cate != null && !cate.isEmpty()) {
            input.append("&CATE=").append(cate);
        } else {
            input.append("&CATE=");
        }
        if (properties != null && !properties.isEmpty()) {
            for (Map.Entry<String, String> en : properties.entrySet()) {
                String object = en.getKey();
                String object1 = en.getValue();
                input.append("&").append(object).append("=").append(object1);
            }
        }
        log.info("==============>>>>> BUILD DATA TO SEND TO MPS<<===============");
        log.info("step 1: data input  -------->" + input);

        String input_with_key_AES = "value=" + encryptAES(input.toString(), keyAES) + "&key=" + keyAES;
        log.info("step 2: decode AES for input ------->" + input_with_key_AES);

        String data_encrypted_RSA = (encryptRSA(input_with_key_AES, publicKeyViettel));
        log.info("step 3: input add RSA for input --->" + data_encrypted_RSA);

        String signature = URLEncoder.encode(createMsgSignature(data_encrypted_RSA, privateKeyCP));
        log.info("step 4: create signature for authentication ---> " + signature);

        boolean kq = verifyMsgSignature(URLDecoder.decode(signature), publicKeyCP, data_encrypted_RSA);
        log.info("step 5: authentication signature ----------> " + kq);

        String url_request = url_wap_mobile + "PRO=" + PRO + "&SER=" + SER + "&SUB=" + SUB + "&CMD="
                + CMD + "&DATA=" + URLEncoder.encode(data_encrypted_RSA) + "&SIG=" + signature;
        return url_request;
    }
}
