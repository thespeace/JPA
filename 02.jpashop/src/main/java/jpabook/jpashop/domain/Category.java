package jpabook.jpashop.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.*;

@Entity
public class Category extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PARENT_ID")
    private Category parent;

    @OneToMany(mappedBy = "parent") //Defalut : LAZY
    private List<Category> child = new ArrayList<>();

    /**
     * <h2>N:M 관계는 1:N, N:1로 변경하라</h2>
     * <ul>
     *     <li>예시이기 때문에 다대다 관계를 사용했지만, 테이블의 N:M 관계는 중간 테이블을 이용해서 1:N, N:1 관계로 변경하라.</li>
     *     <li>실전에서는 중간 테이블 사용이 단순하지 않다.</li>
     *     <li>@ManyToMany는 제약: 필드 추가X, 엔티티 테이블 불일치.</li>
     *     <li>실전에서는 @ManyToMany 사용하지 말자.</li>
     * </ul>
     */
    @ManyToMany
    @JoinTable(name = "CATEGORY_ITEM",
            joinColumns = @JoinColumn(name = "CATEGORY_ID"),
            inverseJoinColumns = @JoinColumn(name = "ITEM_ID")
    )
    private List<Item> items = new ArrayList<>();
}
