package com.motivank.kopringjwt.common.service

import com.motivank.kopringjwt.member.entity.Member
import com.motivank.kopringjwt.member.repository.MemberRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails =
        memberRepository.findByLoginId(username!!)?.let {
            createUserDetails(it)
        } ?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다.")

    private fun createUserDetails(member: Member): UserDetails {
        println(passwordEncoder.encode(member.password))
        return User(
            member.loginId,
//            passwordEncoder.encode(member.password),
            member.password,
            member.memberRole!!.map { SimpleGrantedAuthority("ROLE_${it.role}") }
        )
    }

}