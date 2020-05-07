package util;

import HomomorphicEncryption.User;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class UseBiginteger {
    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        User user = new User("A","B");

//
//        System.out.println(new BigInteger("1258066797475427499047430556236").toString(16));
//        System.out.println(new BigInteger("186589238341839254782869211576").toString(16));
//        System.out.println(new BigInteger("104704643260399961202047764941").toString(16));
//        //sha256 -> 결과값 128자리 (16진수)
//
//        /* testInvert결론 및 해결해야 할 부분
//         * 1. 한글의 경우 str-> hex str로 바꾼 후 다시 한글 str로 변환 과정 추가 필요 (그냥은 한글이 안나옴 -> 인코딩 디코딩문제)
//         * 2. hex str-> byteArray로 바꾸는 부분에서 0으로 시작하면 0이 날라감 -> 최종 비교할 때 결과값에 영향을 줄 수도 있음
//         */
//        testInvert("염상희");
//
//        /*
//         * 한글 쓸려면 StringToHex(str)써 놓은거 쓰면 됨 (아래 코드처럼)
//         * BigInteger big = new BigInteger(StringToHex(str),16);
//         */
//        /* bigInteger상 16진수 사칙연산 테스트
//            bigInteger는 16진수를 10진수로 저장을 하고, 연산도 10진수로 계산함
//            결과를 16진수로 원하면 바꿔서 출력하면 됨
//            1. 덧셈 (.add(BigInteger))-> 16진수 + 10진수 이런 것도 다 가능
//            2. 뺄셈 -> 귀찮아서 생략,,,
//            3. 곱셈 (.multiply(BigInteger))-> 16진수 * 16진수, 16진수 * 10진수 값 같은 것 확인 끝!
//            4. 나눗셈 (.divideAndRemainder(BigInteger))-> divideAndRemainder 쓰면 bigInteger[]로 저장 -> [0]은 몫, [1]은 나머지
//            * mod 연산도 내장되어 있음 *
//            *숫자가 큰 경우에는 아예 bigInteger로 되어있어야 함*
//         */
//        String str = "염상희";
//        BigInteger big = new BigInteger(StringToHex(str),16);
//        int addValue = 100;
//        BigInteger addBig= new BigInteger("1217689356359548");
//
//        System.out.println("---- 덧셈 계산 (with int - 작은 수) ----");
//        System.out.println("기존 값 : " + big + ", " + big.toString(16));
//        System.out.println(big + "+ " + addValue + "=" + big.add(BigInteger.valueOf(16)));
//        big = big.add(BigInteger.valueOf(addValue));//결과값을 저장해야 값이 바뀜
//        System.out.println("결과 값 :" + big + ", " + big.toString(16));
//
//        System.out.println("---- 덧셈 계산 (with bigInteger - 큰 수) ----");
//        System.out.println("기존 값 : " + big + ", " + big.toString(16));
//        System.out.println(big.toString(16) + "+ " + addBig.toString(16) + "=" + big.add(addBig));
//        big = big.add(addBig);//결과값을 저장해야 값이 바뀜
//        System.out.println("결과 값 :" + big + ", " + big.toString(16));
//
//        //곱셈
//        BigInteger multiBig= new BigInteger("2513");
//        System.out.println("---- 곱셈 계산 (with bigInteger - 큰 수) ----");
//        System.out.println("기존 값 : " + big + ", " + big.toString(16));
//        System.out.println("10진수 표시 : "+big.toString() + " * " + multiBig.toString() + "=" + big.multiply(multiBig));
//        System.out.println("16진수 표시 : "+big.toString(16) + " * " + multiBig.toString(16) + "=" + big.multiply(multiBig));
//        big = big.multiply(multiBig);//결과값을 저장해야 값이 바뀜
//        System.out.println("결과 값 :" + big + ", " + big.toString(16));
//
//        //나눗셈
//        BigInteger divBig= new BigInteger("2508924");
//        BigInteger div[] = big.divideAndRemainder(divBig);
//        System.out.println("---- 나눗셈 (with bigInteger - 큰 수) ----");
//        System.out.println("기존 값 : " + big + ", " + big.toString(16));
//        System.out.println("10진수 표시 : "+big.toString() + " / " + divBig.toString() + "=" + div[0] );
//        System.out.println("10진수 표시 : "+big.toString() + " % " + divBig.toString() + "=" + div[1] );
    }

    public static void testInvert(String str){
        System.out.println("----한글 string 출력----");
        System.out.println(str);

        System.out.println("----hexText 출력----");
        String hexText = StringToHex(str);
        System.out.println(hexText);

        System.out.println("----hexText-> byteArray 출력 : 1. byteArray   2. toString----");
        byte byteArray[] = new BigInteger(hexText,16).toByteArray();
        System.out.println(byteArray);
        System.out.println(byteArray.toString()); //string으로 변환 X

        System.out.println("----hexText-> byteArray -> invertHexText 출력----");
        String invertHexText = new BigInteger(byteArray).toString(16);
        System.out.println(invertHexText);

        System.out.println("----hexText to BigInteger : toString----");
        System.out.println(new BigInteger(hexText,16));
        System.out.println(new BigInteger(hexText,16).toString(16));

        System.out.println("----inverthexText to BigInteger : toString----");
        System.out.println(new BigInteger(invertHexText,16).toByteArray().toString());
        System.out.println(new BigInteger(invertHexText,16).toString(16));

    }
    public static String StringToHex(String str) {
        String result = "";
        for(int i=0;i<str.length();i++){
            result += String.format("%02x", (int)str.charAt(i));
        }
        return result;
    }

}
