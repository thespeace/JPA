package hellojpa;

import jakarta.persistence.*;

import java.util.Date;

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

    @Column(name = "username") // 컬럼명도 지정하여 매핑 가능
    private String name;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Lob
    private String description;

    public Member() {
    }

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
