package com.ajaysmarketplace.user

import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRepository : CrudRepository<User, UUID> {

    fun findByEmail(email: String): User?
    // Spring reads this method name and generates
    // select * from users where email = ?

    fun existsByEmail(email: String): Boolean
    //generates : select count(*) > 0 from users where email = ?

}

// interface instead of a class because spring data generates the implementation at runtime
// define only what we need , spring writes the sql , the method name is the query

// crud repository is a repository for user objects whose primary key type is uuid, it gives you save(), findbyid() , findall(), dleetebyId() for free
