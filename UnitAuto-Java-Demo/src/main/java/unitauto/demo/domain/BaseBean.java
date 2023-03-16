package unitauto.demo.domain;

import java.io.Serializable;
import java.util.Date;

public class BaseBean<T extends BaseBean> implements Serializable, Comparable<T> {
    protected Long id; //主键
    protected Date date; //评论时间

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }


    @Override
    public int compareTo(T o) {
        return o == null || id > o.getId() ? 1 : (id == o.getId() ? 0 : -1);
    }


}
