package com.personal_baseball.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"platform_type","email"})
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    //플랫폼이 제공하는 유니크한 키
    @Column(name = "platform_id")
    private String platformId;

    @Column(name = "platform_type")
    private String platformType;

    @Column(name = "email")
    private String email;

    @Column(name = "user_name")
    private String userName;


}
