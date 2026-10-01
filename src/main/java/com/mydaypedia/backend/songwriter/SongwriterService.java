package com.mydaypedia.backend.songwriter;

import com.mydaypedia.backend.common.exception.ResourceNotFoundException;
import com.mydaypedia.backend.songwriter.dto.SongwriterCreateRequest;
import com.mydaypedia.backend.songwriter.dto.SongwriterGroupResponse;
import com.mydaypedia.backend.songwriter.dto.SongwriterMembersRequest;
import com.mydaypedia.backend.songwriter.dto.SongwriterResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SongwriterService {

    private final SongwriterRepository songwriterRepository;

    @Transactional
    public SongwriterResponse create(SongwriterCreateRequest request) {
        Songwriter songwriter = songwriterRepository.save(new Songwriter(request.name(), request.bugsArtistId()));
        return SongwriterResponse.from(songwriter);
    }

    public List<SongwriterResponse> list() {
        return songwriterRepository.findAllByOrderByNameAsc().stream()
                .map(SongwriterResponse::from)
                .toList();
    }

    public SongwriterGroupResponse getMembers(Long id) {
        return SongwriterGroupResponse.from(find(id));
    }

    @Transactional
    public SongwriterGroupResponse replaceMembers(Long id, SongwriterMembersRequest request) {
        Songwriter group = find(id);
        List<Long> memberIds = request.memberIds().stream().distinct().toList();
        if (memberIds.contains(id)) {
            throw new IllegalArgumentException("자기 자신을 구성원으로 넣을 수 없습니다. id=" + id);
        }
        List<Songwriter> members = songwriterRepository.findAllById(memberIds);
        if (members.size() != memberIds.size()) {
            throw new ResourceNotFoundException("존재하지 않는 작곡가/작사가 id가 포함되어 있습니다.");
        }
        group.replaceMembers(members);
        return SongwriterGroupResponse.from(group);
    }

    @Transactional
    public void delete(Long id) {
        if (!songwriterRepository.existsById(id)) {
            throw new ResourceNotFoundException("작곡가/작사가를 찾을 수 없습니다. id=" + id);
        }
        songwriterRepository.deleteById(id);
    }

    private Songwriter find(Long id) {
        return songwriterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("작곡가/작사가를 찾을 수 없습니다. id=" + id));
    }
}
