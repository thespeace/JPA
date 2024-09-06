package hellojpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * <pre>{@code
 * create table Member (
 *     id bigInt not null,
 *     name varchar(255),
 *     primary key (id)
 * );
 * }</pre>
 */
@Entity //JPA가 관리할 객체
@Table(name = "Member") // default = class명 / table명이 다를 경우 지정하여 매핑 가능
public class Member {

    @Id //DB PK와 매핑
    private Long id;

    @Column(name = "name") // 컬럼명도 지정하여 매핑 가능
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
