package com.borderlessteamwork.sixpeng.domain.project.repository;

import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectMember;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    boolean existsByProjectIdAndMemberId(Long projectId, Long memberId);

    Optional<ProjectMember> findByProjectIdAndMemberId(Long projectId, Long memberId);

    /** 프로젝트 삭제 시 참여자 행을 정리하는 용도. */
    void deleteByProjectId(Long projectId);

    // createdBy 는 id 만 쓰므로 프록시 그대로 두고 fetch 하지 않는다.
    @Query("""
            select p
            from ProjectMember pm
            join pm.project p
            where pm.member.id = :memberId
            order by p.id desc
            """)
    List<Project> findProjectsByMemberId(@Param("memberId") Long memberId);

    /**
     * 상태로 거른 목록. 위 메서드와 합쳐 {@code (:status is null or ...)} 로 쓸 수도 있지만,
     * null 파라미터는 타입 추론이 애매해질 수 있어 쿼리를 나눠 둔다.
     */
    @Query("""
            select p
            from ProjectMember pm
            join pm.project p
            where pm.member.id = :memberId
              and p.status = :status
            order by p.id desc
            """)
    List<Project> findProjectsByMemberIdAndStatus(@Param("memberId") Long memberId,
                                                  @Param("status") ProjectStatus status);

    @Query("""
            select pm
            from ProjectMember pm
            join fetch pm.member
            where pm.project.id = :projectId
            order by pm.id
            """)
    List<ProjectMember> findAllByProjectIdWithMember(@Param("projectId") Long projectId);
}
