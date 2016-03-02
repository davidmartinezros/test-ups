package gov.max.service.file.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UploadCompleteEventHandler implements ApplicationListener<UploadComplete> {

//    @Autowired
//    private CommentRepository commentRepository;

    @Override
    public void onApplicationEvent(UploadComplete uploadCompleted) {
//        commentRepository.save(uploadCompleted.getComment());
    }
}
