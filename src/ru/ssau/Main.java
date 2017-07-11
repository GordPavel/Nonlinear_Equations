package ru.ssau;

import javafx.util.Pair;
import ru.ssau.bigdecimalmath.BigDecimalMath;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static ru.ssau.bigdecimalmath.BigDecimalMath.cos;
import static ru.ssau.bigdecimalmath.BigDecimalMath.sin;

public class Main{

    public static void main( String[] args ) throws IOException{
//        max value is 1e7, instead too long time execution
        BigDecimal a         = 12.6;
        Integer    precision = 14, countOfRoots = 100;
        BigDecimal two = 2, pi = BigDecimalMath.pi( precision , RoundingMode.HALF_EVEN ), eps = BigDecimal.valueOf(
                Double.valueOf( "1e-" + precision.toString() ) );
        Supplier<Stream<Pair<BigDecimal, BigDecimal>>> allRanges = () -> Stream.iterate( 1, integer -> integer + 1 )
//                    π*n/a - π/2a
                .map( integer -> BigDecimal.valueOf( integer ).multiply( pi )
                        .divide( a, BigDecimal.ROUND_HALF_EVEN ).subtract(
                        pi.divide( two.multiply( a ), BigDecimal.ROUND_HALF_EVEN ) ) )
//                just where ( cos( a * x ) )' > 0
                .filter( zeroPoint -> -Math.sin( a.multiply( zeroPoint ).doubleValue() ) > 0 ).map(
                        leftBorder -> new Pair<>( leftBorder, leftBorder.add(
                                two.multiply( pi.divide( a.multiply( two ), BigDecimal.ROUND_HALF_EVEN ) ) ) ) );

        Stream<Pair<BigDecimal, BigDecimal>> rangesMoreThen1 = allRanges.get().filter(
                range -> range.getKey().compareTo( BigDecimal.ONE ) == 1 );
        Set<BigDecimal> roots = new TreeSet<>();
        try{
            rangesMoreThen1.forEach( new Consumer<Pair<BigDecimal, BigDecimal>>(){
                BigDecimal previousSubtractOfRoots = BigDecimal.valueOf( Double.MAX_VALUE );
                @Override
                public void accept( Pair<BigDecimal, BigDecimal> range ){
                    if( roots.size() >= countOfRoots ) throw new LoopBreak( roots.size() + " корней" );
                    BigDecimal leftRoot;
                    if( previousSubtractOfRoots.compareTo( eps ) == -1 ) leftRoot = range.getKey().add(
                            previousSubtractOfRoots.divide( two, 50, BigDecimal.ROUND_HALF_UP ) );
                    else leftRoot = calculate( range.getKey(), a, eps );
                    previousSubtractOfRoots = leftRoot.subtract( range.getKey() );
                    roots.add( leftRoot.setScale( precision, BigDecimal.ROUND_HALF_UP ) );

                    if( roots.size() >= countOfRoots ) throw new LoopBreak( roots.size() + " корней" );
                    BigDecimal rightRoot;
                    if( previousSubtractOfRoots.compareTo( eps ) == -1 ) rightRoot = range.getValue().subtract(
                            previousSubtractOfRoots.divide( two, 50, BigDecimal.ROUND_HALF_UP ) );
                    else rightRoot = calculate( range.getValue(), a, eps );
                    previousSubtractOfRoots = range.getValue().subtract( rightRoot );
                    roots.add( rightRoot.setScale( precision, BigDecimal.ROUND_HALF_UP ));
                }
            } );
        }catch( LoopBreak ignored ){}

        roots.forEach( root -> System.out.println( root.toString().replace( "." , "," ) ) );
    }

        static class LoopBreak extends RuntimeException{
            String message;

            LoopBreak( String message ){
                this.message = message;
            }
        }

    private static Boolean isRangeContainsDot( BigDecimal leftBorder, BigDecimal rightBorder, BigDecimal dot ){
        return leftBorder < dot && dot < rightBorder;
    }

    private static BigDecimal calculate( BigDecimal xn, BigDecimal a, BigDecimal eps ){
        BigDecimal one = 1;
        Function<BigDecimal, BigDecimal> originalFunction = x ->
                cos( a.multiply( x ).setScale( 50, BigDecimal.ROUND_HALF_EVEN ) ) -
                one.divide( x, 50, BigDecimal.ROUND_HALF_EVEN );
        Function<BigDecimal, BigDecimal> derivative = x ->
                -a * sin( a.multiply( x ).setScale( 50, BigDecimal.ROUND_HALF_EVEN ) ) +
                one.divide( x.pow( 2 ), 50, BigDecimal.ROUND_HALF_EVEN );
        BigDecimal nextStep = originalFunction.apply( xn ).divide( derivative.apply( xn ), BigDecimal.ROUND_HALF_EVEN );
        BigDecimal xn_1     = xn - nextStep;
        if( nextStep.abs() < eps ) return xn_1;
        return calculate( xn_1, a, eps );
    }
}

