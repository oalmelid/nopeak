package logo;

import java.util.Map;

import score.Score;
import java.util.*;
import java.util.stream.Collectors;

public class LogoOld {

    private int[][] pwm; // ATGCN

    private static Map<Character, Integer> mapping = new TreeMap<>();

    static {
        mapping.put('a', 3);
        mapping.put('t', 0);
        mapping.put('g', 1);
        mapping.put('c', 2);
        mapping.put('n', 4);
    }

    public LogoOld(List<String> kmers) {


        pwm = new int[kmers.get(0).length()][5];


        for(String kmer: kmers){
            char[] charArray = kmer.toCharArray();

            for (int i = 0; i < charArray.length; i++) {
                char b = charArray[i];
                pwm[i][mapping.get(b)] += 1;
            }
        }
    }


    public LogoOld(String base, List<Score> kmers) {
        base = base.replaceAll("n", "");

        List<Character> bases = new ArrayList<>(mapping.keySet());
        Map<String, Integer> known = kmers.stream().collect(Collectors.toMap(Score::getQmer, s -> (int) (s.getHeight())));
        pwm = new int[base.length()][5];

        for (int i = 0; i < base.length(); i++) {
            for (char c : bases) {
                char[] basearray = base.toCharArray();
                basearray[i] = c;
                int height = known.getOrDefault(String.valueOf(basearray),0);

                if(height > 2)
                    pwm[i][mapping.get(c)] = height;
                else pwm[i][mapping.get(c)] = 0;
            }
        }
    }

    @Override
    public String toString(){

        StringBuilder r = new StringBuilder("[");

        for (int[] aPwm : pwm) {
            r.append("[");

            for (int i = 0; i < 5; i++) {
                r.append(aPwm[i]);
                r.append(",");
            }

            r.append("],");
        }

        r.append("]");

        return r.toString();
    }

    public String otherToString(){
        String r = "";

        r += ("a\tt\tg\tc\tn\n");

        for(int j = 0; j < pwm.length; j++) {
            r += j + "\t";
            for (int i = 0; i < 5; i++) {
                r += pwm[j][i];
                r += "\t";
            }
            r += "\n";
        }

        return r;
    }


    public int[][] getPwm(){
        return pwm;
    }

}
