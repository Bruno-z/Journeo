package com.journeo.service;

import com.journeo.dto.CommentRequestDTO;
import com.journeo.dto.CommentResponseDTO;
import com.journeo.exception.ResourceNotFoundException;
import com.journeo.model.Comment;
import com.journeo.model.Guide;
import com.journeo.model.User;
import com.journeo.repository.CommentRepository;
import com.journeo.repository.GuideRepository;
import com.journeo.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final GuideRepository guideRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          GuideRepository guideRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.guideRepository = guideRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentResponseDTO addComment(Long guideId, CommentRequestDTO dto, String authorEmail) {
        Guide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found with id: " + guideId));
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authorEmail));

        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setRating(dto.getRating());
        comment.setGuide(guide);
        comment.setAuthor(author);

        return new CommentResponseDTO(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsForGuide(Long guideId) {
        if (!guideRepository.existsById(guideId)) {
            throw new ResourceNotFoundException("Guide not found with id: " + guideId);
        }
        return commentRepository.findByGuideIdOrderByCreatedAtDesc(guideId)
                .stream()
                .map(CommentResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId, String requestingEmail, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!isAdmin && !comment.getAuthor().getEmail().equals(requestingEmail)) {
            throw new AccessDeniedException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long guideId) {
        return commentRepository.findAverageRatingByGuideId(guideId);
    }
}
