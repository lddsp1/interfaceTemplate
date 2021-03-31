package priv.znd.util.xmlutil;

import org.apache.commons.lang3.StringUtils;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DifferenceEvaluator;

import static org.xmlunit.diff.ComparisonResult.*;

/**
 * @author lddsp
 * @date 2021/3/30 20:49
 */
public class CunstomXmlDvaluator implements DifferenceEvaluator {
        /**
         * 对预期参数值进行比较，如expect不存在值，actual存在则为真
         * @param comparison 比较器
         * @param outcome 比较结果
         * @return
         */
        @Override
        public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
          //  System.out.println(comparison.getControlDetails().getValue()+"||"+comparison.getTestDetails().getValue());
            //System.out.println("=======");
            if(DIFFERENT.equals(outcome)) {
              //  String a = String.valueOf(comparison.getControlDetails().getValue());
                Integer a = 0 ;
                Integer b = 0 ;

               /*if(StringUtils.isEmpty(a) &&
                       !StringUtils.isEmpty(String.valueOf(comparison.getTestDetails().getValue()))){
                  // outcome = SIMILAR;
                }*/
                if (comparison.getControlDetails().getValue() instanceof Integer) {
                    a = (Integer) comparison.getControlDetails().getValue();
                  //  System.out.println("====" + a);
                }
                if(comparison.getTestDetails().getValue() instanceof Integer){
                    b = (Integer) comparison.getTestDetails().getValue();
                  //  System.out.println("====" + b);
                }
               if (("CHILD_NODELIST_LENGTH".equals(comparison.getType().toString()) && a < b) ||
                        (("CHILD_NODELIST_SEQUENCE".equals(comparison.getType().toString()))) ||
                        (("CHILD_LOOKUP".equals(comparison.getType().toString()))
                                && comparison.getControlDetails().getXPath() == null)) {
                   outcome = SIMILAR;
                }
            }
            return outcome;

        }
    }

