package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.shift.*;
import com.invoice.invoice_api.enums.*;
import com.invoice.invoice_api.exception.*;
import com.invoice.invoice_api.mapper.*;
import com.invoice.invoice_api.model.*;
import com.invoice.invoice_api.repository.*;
import com.invoice.invoice_api.security.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class ShiftService {
    private final ShiftRepository shifts; private final ShiftAssignmentRepository assignments; private final CompanyRepository companies; private final ProjectPositionRepository positions; private final WorkerProfileRepository workers; private final CompanyMembershipRepository memberships; private final AuthenticatedUserService auth; private final CompanyContext context; private final NotificationService notifications;
    public ShiftService(ShiftRepository shifts, ShiftAssignmentRepository assignments, CompanyRepository companies, ProjectPositionRepository positions, WorkerProfileRepository workers, CompanyMembershipRepository memberships, AuthenticatedUserService auth, CompanyContext context, NotificationService notifications){this.shifts=shifts;this.assignments=assignments;this.companies=companies;this.positions=positions;this.workers=workers;this.memberships=memberships;this.auth=auth;this.context=context;this.notifications=notifications;}

    @Transactional
    public ShiftResponseDTO create(Long companyId, ShiftRequestDTO request){
        requireManager(companyId); validateRequest(request);
        Company company=activeCompany(companyId);
        ProjectPosition position=positions.findByIdAndCompanyIdForUpdate(request.projectPositionId(),companyId).orElseThrow(()->new ResourceNotFoundException("Project position not found."));
        if(!Boolean.TRUE.equals(position.getActive())||!Boolean.TRUE.equals(position.getProject().getActive())) throw new InvalidOperationException("Project position is inactive.");
        Shift shift=new Shift(); shift.setCompany(company); shift.setProjectPosition(position); shift.setCreatedBy(auth.getCurrentUser()); shift.setMode(request.mode()); shift.setStatus(ShiftStatus.OPEN); shift.setShiftDate(request.shiftDate()); shift.setStartTime(request.startTime()); shift.setEndTime(request.endTime()); shift.setCapacity(request.mode()==ShiftMode.INDIVIDUAL?1:request.capacity()); shift.setLocation(request.location()); shift.setNotes(request.notes());
        if(request.mode()==ShiftMode.INDIVIDUAL){
            if(request.workerProfileId()==null) throw new BusinessException("workerProfileId is required for an individual shift.");
            workerForCompany(request.workerProfileId(),companyId);
        } else if(request.workerProfileId()!=null) throw new BusinessException("workerProfileId is not allowed for a public shift.");
        Shift saved=shifts.save(shift);
        if(request.mode()==ShiftMode.INDIVIDUAL){ WorkerProfile worker = workerForCompany(request.workerProfileId(),companyId); ShiftAssignment assignment=new ShiftAssignment(); assignment.setShift(saved); assignment.setWorkerProfile(worker); assignment.setStatus(ShiftAssignmentStatus.PENDING); assignments.save(assignment); notifications.create(worker.getAppUser(), company, NotificationType.SHIFT_ASSIGNED, "You have been assigned a shift", shiftSummary(saved), saved.getId()); }
        else { workers.findWorkersByCompanyIdAndMembershipStatuses(companyId, List.of(MembershipStatus.ACTIVE)).forEach(worker -> notifications.create(worker.getAppUser(), company, NotificationType.SHIFT_AVAILABLE, "A new shift is available", shiftSummary(saved), saved.getId())); }
        return toResponse(saved, true, null);
    }

    @Transactional(readOnly = true)
    public List<ShiftResponseDTO> listForAdmin(Long companyId){ requireManager(companyId); activeCompany(companyId); return shifts.findByCompanyIdOrderByShiftDateAscStartTimeAsc(companyId).stream().map(s->toResponse(s,true,null)).toList(); }

    @Transactional
    public ShiftResponseDTO find(Long companyId,Long shiftId){ requireMember(companyId); Shift shift=shift(shiftId,companyId); Long workerId=isWorker()?currentWorkerProfileId():null; return toResponse(shift, isManager(), workerId); }

    @Transactional
    public List<ShiftResponseDTO> available(Long companyId){ requireWorker(companyId); activeCompany(companyId); Long workerId=currentWorkerProfileId(); return shifts.findByCompanyIdAndStatusOrderByShiftDateAscStartTimeAsc(companyId,ShiftStatus.OPEN).stream().filter(s->!expired(s)).filter(s->s.getMode()==ShiftMode.PUBLIC || assignments.findByShiftIdAndWorkerProfileId(s.getId(),workerId).isPresent()).map(s->toResponse(s,false,workerId)).toList(); }

    @Transactional
    public List<ShiftResponseDTO> mine(Long companyId){ requireWorker(companyId); activeCompany(companyId); Long workerId=currentWorkerProfileId(); return assignments.findByWorkerProfileIdAndStatusInOrderByCreatedAtDesc(workerId,List.of(ShiftAssignmentStatus.PENDING,ShiftAssignmentStatus.ACCEPTED,ShiftAssignmentStatus.DECLINED,ShiftAssignmentStatus.CANCELLED)).stream().filter(a->a.getShift().getCompany().getId().equals(companyId)).map(a->toResponse(a.getShift(),false,workerId)).toList(); }

    @Transactional
    public ShiftResponseDTO accept(Long companyId,Long shiftId){ requireWorker(companyId); activeCompany(companyId); Long workerId=currentWorkerProfileId(); Shift shift=shifts.findByIdAndCompanyIdForUpdate(shiftId,companyId).orElseThrow(()->new ResourceNotFoundException("Shift not found.")); refreshStatus(shift); if(shift.getStatus()!=ShiftStatus.OPEN) throw new InvalidOperationException("Shift is no longer available."); if(shift.getShiftDate().isBefore(LocalDate.now())) throw new InvalidOperationException("Shift has already started or expired."); ShiftAssignment assignment=assignments.findByShiftIdAndWorkerProfileId(shiftId,workerId).orElseGet(()->{ if(shift.getMode()!=ShiftMode.PUBLIC) throw new AccessDeniedBusinessException("This shift was not assigned to you."); ShiftAssignment a=new ShiftAssignment();a.setShift(shift);a.setWorkerProfile(workerForCompany(workerId,companyId));return a; }); if(assignment.getStatus()==ShiftAssignmentStatus.ACCEPTED) return toResponse(shift,false,workerId); if(assignment.getStatus()==ShiftAssignmentStatus.DECLINED||assignment.getStatus()==ShiftAssignmentStatus.CANCELLED) throw new InvalidOperationException("This shift response cannot be changed."); if(assignments.countByShiftIdAndStatus(shiftId,ShiftAssignmentStatus.ACCEPTED)>=shift.getCapacity()) {shift.setStatus(ShiftStatus.FULL);shifts.save(shift);throw new InvalidOperationException("No vacancies remain.");} assignment.setStatus(ShiftAssignmentStatus.ACCEPTED); assignment.setRespondedAt(LocalDateTime.now()); assignment.setDeclineReason(null); assignments.save(assignment); if(assignments.countByShiftIdAndStatus(shiftId,ShiftAssignmentStatus.ACCEPTED)>=shift.getCapacity()) shift.setStatus(ShiftStatus.FULL); shifts.save(shift); notifications.createWithoutEmail(assignment.getWorkerProfile().getAppUser(), shift.getCompany(), NotificationType.SHIFT_ACCEPTED, "Shift accepted", shiftSummary(shift), shift.getId()); return toResponse(shift,false,workerId); }

    @Transactional
    public ShiftResponseDTO decline(Long companyId,Long shiftId,ShiftDeclineRequestDTO request){ requireWorker(companyId); activeCompany(companyId); Long workerId=currentWorkerProfileId(); Shift shift=shift(shiftId,companyId); ShiftAssignment assignment=assignments.findByShiftIdAndWorkerProfileId(shiftId,workerId).orElseThrow(()->new AccessDeniedBusinessException("This shift is not assigned to you.")); if(assignment.getStatus()!=ShiftAssignmentStatus.PENDING) throw new InvalidOperationException("Only pending shifts can be declined."); assignment.setStatus(ShiftAssignmentStatus.DECLINED); assignment.setDeclineReason(request==null?null:request.reason()); assignment.setRespondedAt(LocalDateTime.now()); assignments.save(assignment); notifications.create(assignment.getWorkerProfile().getAppUser(), shift.getCompany(), NotificationType.SHIFT_DECLINED, "Shift declined", shiftSummary(shift), shift.getId()); return toResponse(shift,false,workerId); }

    @Transactional
    public ShiftResponseDTO cancelAssignment(Long companyId, Long shiftId, Long assignmentId) {
        requireOwner(companyId);
        Company company = activeCompany(companyId);
        Shift shift = shifts.findByIdAndCompanyIdForUpdate(shiftId, companyId).orElseThrow(() -> new ResourceNotFoundException("Shift not found."));
        if (!LocalDateTime.of(shift.getShiftDate(), shift.getStartTime()).isAfter(LocalDateTime.now())) throw new InvalidOperationException("A worker assignment can only be cancelled before the shift starts.");
        ShiftAssignment assignment = assignments.findByIdAndShiftIdAndCompanyIdForUpdate(assignmentId, shiftId, companyId).orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found."));
        if (assignment.getStatus() != ShiftAssignmentStatus.PENDING && assignment.getStatus() != ShiftAssignmentStatus.ACCEPTED) throw new InvalidOperationException("Only pending or accepted assignments can be cancelled.");
        assignment.setStatus(ShiftAssignmentStatus.CANCELLED);
        assignment.setRespondedAt(LocalDateTime.now());
        assignments.save(assignment);
        if (shift.getStatus() == ShiftStatus.FULL && assignments.countByShiftIdAndStatus(shiftId, ShiftAssignmentStatus.ACCEPTED) < shift.getCapacity()) { shift.setStatus(ShiftStatus.OPEN); shifts.save(shift); }
        notifications.create(assignment.getWorkerProfile().getAppUser(), company, NotificationType.SHIFT_CANCELLED, "Shift assignment cancelled", "Your assignment for " + shiftSummary(shift) + " was cancelled by the company.", shift.getId());
        return toResponse(shift, true, null);
    }

    @Transactional
    public void cancel(Long companyId,Long shiftId){ requireManager(companyId); activeCompany(companyId); Shift shift=shifts.findByIdAndCompanyIdForUpdate(shiftId,companyId).orElseThrow(()->new ResourceNotFoundException("Shift not found.")); if(!LocalDateTime.of(shift.getShiftDate(),shift.getStartTime()).isAfter(LocalDateTime.now())) throw new InvalidOperationException("A shift can only be cancelled before it starts."); shift.setStatus(ShiftStatus.CANCELLED); shifts.save(shift); assignments.findByShiftIdOrderByCreatedAtAsc(shiftId).stream().filter(a->a.getStatus()==ShiftAssignmentStatus.PENDING||a.getStatus()==ShiftAssignmentStatus.ACCEPTED).forEach(a->{a.setStatus(ShiftAssignmentStatus.CANCELLED);a.setRespondedAt(LocalDateTime.now());assignments.save(a);notifications.create(a.getWorkerProfile().getAppUser(), shift.getCompany(), NotificationType.SHIFT_CANCELLED, "Shift cancelled", shiftSummary(shift), shift.getId());}); }

    private Shift shift(Long id,Long companyId){return shifts.findById(id).filter(s->s.getCompany().getId().equals(companyId)).orElseThrow(()->new ResourceNotFoundException("Shift not found."));}
    private Company activeCompany(Long id){return companies.findById(id).filter(c->Boolean.TRUE.equals(c.getActive())).orElseThrow(()->new ResourceNotFoundException("Company not found."));}
    private WorkerProfile workerForCompany(Long profileId,Long companyId){WorkerProfile p=workers.findById(profileId).orElseThrow(()->new ResourceNotFoundException("Worker not found.")); memberships.findByAppUserIdAndCompanyIdAndStatus(p.getAppUser().getId(),companyId,MembershipStatus.ACTIVE).filter(m->m.getRole()==CompanyRole.WORKER).orElseThrow(()->new ResourceNotFoundException("Worker not found.")); return p;}
    private Long currentWorkerProfileId(){return workers.findByAppUserId(auth.getCurrentUserId()).map(WorkerProfile::getId).orElseThrow(()->new ResourceNotFoundException("Worker profile not found."));}
    private boolean isManager(){try{CompanyRole role=context.getRole();return role==CompanyRole.OWNER||role==CompanyRole.ADMIN||role==CompanyRole.MANAGER;}catch(RuntimeException e){return false;}}
    private boolean isWorker(){try{return context.getRole()==CompanyRole.WORKER;}catch(RuntimeException e){return false;}}
    private String shiftSummary(Shift shift){return shift.getProjectPosition().getProject().getName()+" · "+shift.getProjectPosition().getPositionName()+" on "+shift.getShiftDate()+" from "+shift.getStartTime()+" to "+shift.getEndTime()+".";}
    private void requireMember(Long companyId){activeCompany(companyId);if(!companyId.equals(context.getCompanyId()))throw new AccessDeniedBusinessException("The selected company does not match the request.");}
    private void requireWorker(Long companyId){requireMember(companyId);if(context.getRole()!=CompanyRole.WORKER)throw new AccessDeniedBusinessException("Worker permission required.");}
    private void requireManager(Long companyId){requireMember(companyId);CompanyRole r=context.getRole();if(r!=CompanyRole.OWNER&&r!=CompanyRole.ADMIN&&r!=CompanyRole.MANAGER)throw new AccessDeniedBusinessException("Shift management permission required.");}
    private void requireOwner(Long companyId){requireMember(companyId);if(context.getRole()!=CompanyRole.OWNER)throw new AccessDeniedBusinessException("Only the company owner can cancel a worker assignment.");}
    private void validateRequest(ShiftRequestDTO r){if(!r.endTime().isAfter(r.startTime()))throw new BusinessException("End time must be after start time.");if(r.mode()==ShiftMode.PUBLIC&&r.capacity()<1)throw new BusinessException("Public shifts require at least one vacancy.");}
    private boolean expired(Shift s){return LocalDateTime.of(s.getShiftDate(),s.getEndTime()).isBefore(LocalDateTime.now());}
    private void refreshStatus(Shift s){if((s.getStatus()==ShiftStatus.OPEN||s.getStatus()==ShiftStatus.FULL)&&expired(s)){s.setStatus(ShiftStatus.EXPIRED);shifts.save(s);}}
    private ShiftResponseDTO toResponse(Shift s,boolean includeAssignments,Long workerId){refreshStatus(s);List<ShiftAssignment> rows=assignments.findByShiftIdOrderByCreatedAtAsc(s.getId());int accepted=(int)rows.stream().filter(a->a.getStatus()==ShiftAssignmentStatus.ACCEPTED).count();var visible=includeAssignments?rows:rows.stream().filter(a->workerId!=null&&a.getWorkerProfile().getId().equals(workerId)).toList();var mapped=visible.stream().map(a->new ShiftAssignmentResponseDTO(a.getId(),a.getWorkerProfile().getId(),a.getWorkerProfile().getAppUser().getFullName(),a.getStatus(),a.getDeclineReason(),a.getRespondedAt())).toList();ShiftAssignmentStatus mine=workerId==null?null:rows.stream().filter(a->a.getWorkerProfile().getId().equals(workerId)).map(ShiftAssignment::getStatus).findFirst().orElse(null);var p=s.getProjectPosition();return new ShiftResponseDTO(s.getId(),s.getCompany().getId(),p.getId(),p.getProject().getId(),p.getProject().getName(),p.getPositionName(),s.getMode(),s.getStatus(),s.getShiftDate(),s.getStartTime(),s.getEndTime(),s.getCapacity(),accepted,Math.max(0,s.getCapacity()-accepted),s.getLocation(),s.getNotes(),mine,mapped,s.getCreatedAt(),s.getUpdatedAt());}
}
