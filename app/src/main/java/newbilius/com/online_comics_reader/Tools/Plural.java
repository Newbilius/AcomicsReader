package newbilius.com.online_comics_reader.Tools;

public class Plural {
    public static String get(int number,
                             String form1,
                             String form2,
                             String form5) {
        return (number % 10 == 1 && number % 100 != 11)
                ? form1
                : (number % 10 >= 2 && number % 10 <= 4 && (number % 100 < 10 || number % 100 >= 20) ? form2 : form5);
    }
}
