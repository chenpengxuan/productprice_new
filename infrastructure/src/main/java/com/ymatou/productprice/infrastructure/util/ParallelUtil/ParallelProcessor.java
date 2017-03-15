package com.ymatou.productprice.infrastructure.util.ParallelUtil;

import java.util.Collection;
import java.util.concurrent.RecursiveTask;

/**
 * 并行处理器
 * Created by chenpengxuan on 2017/3/15.
 */
public class ParallelProcessor<T extends Collection> {
}

class ParallelTask <T extends Collection> extends RecursiveTask<T>{

    @Override
    protected T compute() {
        return null;
    }
}


