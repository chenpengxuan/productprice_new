package com.ymatou.productprice.infrastructure.util.ParallelUtil;

import com.ymatou.productprice.infrastructure.config.props.BizProps;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import com.ymatou.productprice.infrastructure.util.Utils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * 并行处理器
 * Created by chenpengxuan on 2017/3/15.
 */
@Component
public class ParallelProcessor<T extends List, R extends List> {

    @Autowired
    private BizProps bizProps;

    @Autowired
    private LogWrapper logWrapper;

    private static ParallelTask parallelTask;

    private static ForkJoinPool forkJoinPool;

    private static ThreadPoolExecutor threadPoolExecutor;

    private static ParallelTypeEnum parallelTypeEnum;

    @PostConstruct
    private void init() {
        forkJoinPool = new ForkJoinPool(bizProps.getParallelCount());
        threadPoolExecutor = new ThreadPoolExecutor(bizProps.getParallelCount(),
                bizProps.getParallelCount(),
                Long.MAX_VALUE,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
    }

    /**
     * 并行操作方法
     *
     * @param sourceList
     * @param func
     * @param parallelType
     * @return
     */
    public R doParallelProcess(T sourceList, Function<T, R> func, ParallelTypeEnum parallelType) {
        switch (parallelType) {
            case FORKJOIN:
                parallelTask = new ParallelTask(sourceList, func, bizProps.getParallelThresHoldCount());
                return (R) forkJoinPool.invoke(parallelTask);
            case THREADPOOL:
                R result = (R) Lists.newArrayList();
                List<Future> futureList = Lists.newArrayList();
                List<List<T>> lists = Utils.splitCollectionToCollectionList(sourceList, bizProps.getParallelThresHoldCount());
                lists.stream().forEach(x -> {
                    Future tempFuture = threadPoolExecutor.submit(new ThreadPoolTask(x, func));
                    futureList.add(tempFuture);
                });
                futureList.stream().forEach(r -> {
                    try {
                        result.addAll((R) r.get());
                    } catch (InterruptedException e) {
                        logWrapper.recordErrorLog("并行操作方法_doParallelProcess发生线程中断异常{}", e.getLocalizedMessage(), e);
                    } catch (ExecutionException e) {
                        logWrapper.recordErrorLog("并行操作方法_doParallelProcess发生异常{}", e.getLocalizedMessage(), e);
                    }
                });
                return result;
            default:
                return null;
        }
    }

    /**
     * 并行操作方法
     *
     * @param sourceList
     * @param func
     * @return
     */
    public R doParallelProcess(T sourceList, Function<T, R> func) {
        parallelTypeEnum = Optional.ofNullable(parallelTypeEnum).orElse(ParallelTypeEnum.THREADPOOL);
        return doParallelProcess(sourceList,func,parallelTypeEnum);
    }
}

/**
 * 并行任务
 *
 * @param <T>
 * @param <R>
 */
class ParallelTask<T extends List, R extends List> extends RecursiveTask<R> {
    private T sourceCollection;
    private int parallelTaskThresholdNum;
    private Function<T, R> processFunc;

    public ParallelTask(T collection, Function<T, R> func, int thresholdNum) {
        this.sourceCollection = collection;
        this.parallelTaskThresholdNum = thresholdNum;
        this.processFunc = func;
    }

    @Override
    protected R compute() {
        R result = (R) Lists.newArrayList();
        if (sourceCollection.size() < parallelTaskThresholdNum) {
            result = processFunc.apply(sourceCollection);
        } else {
            int mid = sourceCollection.size() >>> 1;
            List<List> subLists = Utils.splitCollectionToTwoList(sourceCollection, mid);
            ParallelTask left = new ParallelTask(subLists.get(0), processFunc, this.parallelTaskThresholdNum);
            ParallelTask right = new ParallelTask(subLists.get(1), processFunc, this.parallelTaskThresholdNum);
            left.fork();
            right.fork();
            result.addAll((R) left.join());
            result.addAll((R) right.join());
        }
        return result;
    }
}

/**
 * 线程任务
 *
 * @param <T>
 * @param <R>
 */
class ThreadPoolTask<T extends List, R extends List> implements Callable<R> {

    private T sourceCollection;
    private Function<T, R> processFunc;

    public ThreadPoolTask(T sourceCollection, Function<T, R> processFunc) {
        this.sourceCollection = sourceCollection;
        this.processFunc = processFunc;
    }

    @Override
    public R call() throws Exception {
        return processFunc.apply(this.sourceCollection);
    }
}


