package ru.ssau;

import io.github.miraclefoxx.math.BigDecimalMath;
import javafx.util.Pair;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Main{

    public static void main( String[] args ) throws IOException{
//        max value is 1e7, instead too long time execution
        BigDecimal a         = 1.3;
        Integer    precision = 14, countOfRoots = 100;
        Boolean    toFile    = false;
        BigDecimal two = 2, pi = BigDecimal.valueOf( Math.PI ), eps = BigDecimal.valueOf(
                Double.valueOf( "1e-" + precision.toString() ) );
        Supplier<Stream<Pair<BigDecimal, BigDecimal>>> allRanges = () -> Stream.iterate( 1, integer -> integer + 1 )
//                    π*n/a - π/2a
                .map( integer -> BigDecimal.valueOf( integer ).multiply( pi ).divide( a,
                                                                                      BigDecimal.ROUND_HALF_EVEN ).subtract(
                        pi.divide( two.multiply( a ), BigDecimal.ROUND_HALF_EVEN ) ) )
//                just where ( cos( a * x ) )' > 0
                .filter( zeroPoint -> -Math.sin( a.multiply( zeroPoint ).doubleValue() ) > 0 ).map(
                        leftBorder -> new Pair<>( leftBorder, leftBorder.add(
                                two.multiply( pi.divide( a.multiply( two ), BigDecimal.ROUND_HALF_EVEN ) ) ) ) );

        Optional<Pair<BigDecimal, BigDecimal>> rangeContains1;
        try{
            rangeContains1 = allRanges.get().filter( range -> {
                if( range.getKey().compareTo( BigDecimal.ONE ) == 1 )
                    throw new LoopBreak( "нет отрезка, содержащего 1" );
                return isRangeContainsDot( range.getKey(), range.getValue(), BigDecimal.ONE );
            } ).findFirst();
        }catch( LoopBreak e ){
            rangeContains1 = Optional.empty();
        }
        if( rangeContains1.isPresent() ){
            BigDecimal leftBorder = rangeContains1.get().getKey(), rightBorder = rangeContains1.get().getValue(), middle
                    = ( leftBorder + rightBorder ).divide( two, 50, BigDecimal.ROUND_HALF_EVEN );
            Function<BigDecimal, BigDecimal> originalFunction = x ->
                    BigDecimalMath.cos( a.multiply( x ).setScale( 50, BigDecimal.ROUND_HALF_EVEN ) ) -
                    BigDecimal.ONE.divide( x, 50, BigDecimal.ROUND_HALF_EVEN );
            System.out.println(
                    countRoots( originalFunction.apply( leftBorder ), originalFunction.apply( middle ), eps ) );
        }

        Stream<Pair<BigDecimal, BigDecimal>> rangesMoreThen1 = allRanges.get().filter(
                range -> range.getKey().compareTo( BigDecimal.ONE ) == 1 );
        PrintStream printStream;
        if( toFile ) printStream = new PrintStream(
                Files.newOutputStream( Paths.get( "/Users/pavelgordeev/Desktop/dots.txt" ),
                                       StandardOpenOption.CREATE_NEW ), true, "utf-8" );
        else printStream = System.out;
        try( PrintStream out = printStream ){
            rangesMoreThen1.forEach( new Consumer<Pair<BigDecimal, BigDecimal>>(){
                Integer i = 0;
                BigDecimal previousSubtractOfRoots = BigDecimal.valueOf( Double.MAX_VALUE );

                @Override
                public void accept( Pair<BigDecimal, BigDecimal> range ){
                    if( i >= countOfRoots ) throw new LoopBreak( i + " корней" );
                    BigDecimal leftRoot;
                    if( previousSubtractOfRoots.compareTo( eps ) == -1 ) leftRoot = range.getKey().add(
                            previousSubtractOfRoots.divide( two, 50, BigDecimal.ROUND_HALF_UP ) );
                    else leftRoot = calculate( range.getKey(), a, eps );
                    previousSubtractOfRoots = leftRoot.subtract( range.getKey() );
                    i++;
                    out.println( leftRoot.setScale( precision, BigDecimal.ROUND_HALF_UP ) + " " + i );

                    if( i >= countOfRoots ) throw new LoopBreak( i + " корней" );
                    BigDecimal rightRoot;
                    if( previousSubtractOfRoots.compareTo( eps ) == -1 ) rightRoot = range.getValue().subtract(
                            previousSubtractOfRoots.divide( two, 50, BigDecimal.ROUND_HALF_UP ) );
                    else rightRoot = calculate( range.getValue(), a, eps );
                    previousSubtractOfRoots = range.getValue().subtract( rightRoot );
                    i++;
                    out.println( rightRoot.setScale( precision, BigDecimal.ROUND_HALF_UP ) + " " + i );
                }
            } );
        }catch( LoopBreak e ){
            System.out.println( e.message );
        }
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

    private static CountOfValues countRoots( BigDecimal valueOfFunctionInBorders, BigDecimal valueOfFunctionInMiddle,
                                             BigDecimal eps ){
        if( valueOfFunctionInMiddle.abs() < eps ) return CountOfValues.One;
        else if( valueOfFunctionInBorders.abs() < eps ) return CountOfValues.Two;
        else if( valueOfFunctionInBorders.signum() * valueOfFunctionInMiddle.signum() == 1 ) return CountOfValues.NoOne;
        else return CountOfValues.Two;
    }

    enum CountOfValues{
        NoOne, One, Two
    }

    private static BigDecimal calculate( BigDecimal xn, BigDecimal a, BigDecimal eps ){
        BigDecimal one = 1;
        Function<BigDecimal, BigDecimal> originalFunction = x ->
                BigDecimalMath.cos( a.multiply( x ).setScale( 50, BigDecimal.ROUND_HALF_EVEN ) ) -
                one.divide( x, 50, BigDecimal.ROUND_HALF_EVEN );
        Function<BigDecimal, BigDecimal> derivative = x ->
                -a * BigDecimalMath.sin( a.multiply( x ).setScale( 50, BigDecimal.ROUND_HALF_EVEN ) ) +
                one.divide( x.pow( 2 ), 50, BigDecimal.ROUND_HALF_EVEN );
        BigDecimal nextStep = originalFunction.apply( xn ).divide( derivative.apply( xn ), BigDecimal.ROUND_HALF_EVEN );
        BigDecimal xn_1     = xn - nextStep;
        if( nextStep.abs() < eps ) return xn_1;
        return calculate( xn_1, a, eps );
    }
}

