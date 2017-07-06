package ru.ssau;

import io.github.miraclefoxx.math.BigDecimalMath;
import javafx.util.Pair;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main{

    public static void main( String[] args ) throws IOException{
        BigDecimal a = 3 * Math.PI / 2;
        Integer precision = 15;
        BigDecimal                        two = 2 , pi = BigDecimal.valueOf( Math.PI );
        List<Pair<BigDecimal, BigDecimal>> ranges = Stream.iterate( 1 , integer -> integer + 1 )
//                    π*n/a - π/2a
                .map( integer -> BigDecimal.valueOf( integer ).multiply( pi ).divide( a , BigDecimal.ROUND_HALF_EVEN )
                        .subtract( pi.divide( two.multiply( a ) , BigDecimal.ROUND_HALF_EVEN ) ) )
//                just where ( cos( a * x ) )' > 0
                .filter( zeroPoint -> -Math.sin( a.multiply( zeroPoint ).doubleValue() ) > 0 )
//                .filter( point -> point.compareTo( BigDecimal.valueOf( 1 ) ) == 1 )
                .map( leftBorder -> new Pair<>( leftBorder , leftBorder.add( two.multiply( pi.divide( a.multiply( two ) , BigDecimal.ROUND_HALF_EVEN ) ) ) ) )
                .limit( 50 )
                .collect( Collectors.toList() );
        for( Pair<BigDecimal, BigDecimal> range : ranges ){
            System.out.printf( "[%s;%s] " , range.getKey().setScale( precision , BigDecimal.ROUND_HALF_UP ) ,
                               range.getValue().setScale( precision , BigDecimal.ROUND_HALF_UP ) );
            System.out.println( isRangeContainsDot( range.getKey() , range.getValue() , BigDecimal.ONE ) );
        }
//        System.setOut( new PrintStream( Files.newOutputStream( Paths.get( "/Users/pavelgordeev/Desktop/dots.txt" ) , StandardOpenOption.CREATE_NEW ) , true , "utf-8" ) );
//        for( Pair<BigDecimal, BigDecimal> range : ranges ){
//            String leftValue = calculate( range.getKey(), a, precision ).toString().replace( ".", "," ),
//                    rightValue = calculate( range.getValue(), a, precision ).toString().replace( ".", "," );
//            System.out.printf( "%s 0 0\n" , leftValue );
//            System.out.printf( "%s 0 0\n" , rightValue );
//        }
    }

    private static Boolean isRangeContainsDot( BigDecimal leftBorder , BigDecimal rightBorder , BigDecimal dot ){
        return leftBorder < dot && dot < rightBorder;
    }

    private static BigDecimal calculate( BigDecimal xn, BigDecimal a, Integer precision ){
        BigDecimal eps = BigDecimal.valueOf( Double.valueOf( "1e-" + precision.toString() ) );
        return calculate( xn , a , eps );
    }

    private static BigDecimal calculate( BigDecimal xn, BigDecimal a, BigDecimal eps ){
        BigDecimal                       one              = 1;
        Function<BigDecimal, BigDecimal> originalFunction = x -> BigDecimalMath.cos( a.multiply( x ).setScale( 50 , BigDecimal.ROUND_HALF_EVEN ) ) - one.divide( x, 50, BigDecimal.ROUND_HALF_EVEN );
        Function<BigDecimal, BigDecimal> derivative       = x -> -a * BigDecimalMath.sin( a.multiply( x ).setScale( 50 , BigDecimal.ROUND_HALF_EVEN ) ) + one.divide( x.pow( 2 ), 50 , BigDecimal.ROUND_HALF_EVEN );
        BigDecimal                       nextStep         = originalFunction.apply( xn ).divide( derivative.apply( xn ), BigDecimal.ROUND_HALF_EVEN );
        BigDecimal                       xn_1             = xn - nextStep;
        if( nextStep.abs() < eps ) return xn_1;
        return calculate( xn_1, a , eps );
    }

    private static CountOfValues countValues( BigDecimal valueOfFunctionInBorders, BigDecimal valueOfFunctionInMiddle,
                                              Integer countOfImportantNumbersAfterDot ){
        BigDecimal eps = BigDecimal.valueOf( Double.valueOf( "1e-" + countOfImportantNumbersAfterDot.toString() ) );
        if( valueOfFunctionInMiddle.abs() < eps  ) return CountOfValues.One;
        else if( valueOfFunctionInBorders.abs() < eps ) return CountOfValues.Two;
        else if( valueOfFunctionInBorders.signum() * valueOfFunctionInMiddle.signum() == 1 ) return CountOfValues.NoOne;
        else return CountOfValues.Two;
    }

    enum CountOfValues{
        NoOne, One, Two
    }
}

