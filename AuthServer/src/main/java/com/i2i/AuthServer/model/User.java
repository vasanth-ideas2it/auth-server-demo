package com.i2i.AuthServer.model;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
@Getter
@Setter
@EnableJpaAuditing
@NoArgsConstructor
@AllArgsConstructor
public class User extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName="id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName="id")}
    )
    private Set<Role> roles = new HashSet<>();


    @PreUpdate
    public void preUpdate() {
        this.setLastModifiedDate(LocalDateTime.now()) ;
    }

    @PrePersist
    public void prePersist() {
        this.setCreatedDate( LocalDateTime.now());
    }
}
