package com.mylog.service.member;


import com.mylog.dto.SignUpRequest;
import com.mylog.dto.UpdateMemberRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;


public interface MemberService {

    void saveMember(SignUpRequest request);

    Member getMember(CustomUser customUser);

    void updateMember(UpdateMemberRequest request, CustomUser customUser, MultipartFile file)
        throws IOException;

    void deleteMember(CustomUser customUser);



}
