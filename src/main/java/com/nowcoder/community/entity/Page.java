package com.nowcoder.community.entity;

/**
 * 封装分页相关的信息(实现分页时页面会传入分页有关的条件，这些条件被Page封装)
 *
 * -page为什么不需要加入类似的component注解使之被自动扫描呢?
 * -page只是一个实体，而且是多例的，每次请求都有不同的数据，需要重置Page。通常，封装逻辑的组件才需要
 *  由容器管理，为了便于复用这个逻辑。
 *
 * -SpringMVC是如何知道地址栏current的值是应该封装到Page这个bean中的呢?
 * -SpringMVC会自动将请求中的参数映射到Controller方法参数上，默认按照同名规则映射，无论是基本类型
 *  与之同名，还是对象中的成员与之同名。
 *
 *
 */
public class Page {

    //当前页码
    private int current = 1;

    //显示上限
    private int limit = 10;

    //数据总数(用于计算总页数)
    private int rows;

    //查询路径(用来复用分页链接)
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current > 1) {
            this.current = current;
        }

    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit > 1 && limit < 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows >= 0) {
            this.rows = rows;
        }

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     * @return
     */
    public int getOffset() {
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal() {
        if(rows % limit == 0) {
            return rows / limit;
        }
        return rows / limit + 1;
    }

    /**
     * 获取起始页码
     * @return
     */
    public int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 获取结束页码
     * @return
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }
}
