import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


public class MD5Test {

    //MD5不能直接经行储存 需要进行盐值加密
    @Test
    public void Md5(){
//        String s = DigestUtils.md5Hex("123465 "); //3d9188577cc9bfe9291ac66b5cc872b7
//        String s1 = Md5Crypt.md5Crypt("123456".getBytes());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); //$2a$10$xQZLRxYyu5G4FhOHKsazbu9YzU7JAySy6.Ltd6O5FyT.qtRU0ETV6
        String s = passwordEncoder.encode("123456");
        boolean matches = passwordEncoder.matches("123456", "$2a$10$xQZLRxYyu5G4FhOHKsazbu9YzU7JAySy6.Ltd6O5FyT.qtRU0ETV6");
        System.out.println(matches);
    }
}
