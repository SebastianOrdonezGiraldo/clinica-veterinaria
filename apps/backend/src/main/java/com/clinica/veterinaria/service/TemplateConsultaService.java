package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.TemplateConsultaDTO;
import com.clinica.veterinaria.entity.TemplateConsulta;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.TemplateConsultaRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TemplateConsultaService {

    private final TemplateConsultaRepository repository;
    private final UsuarioRepository usuarioRepository;

    public List<TemplateConsultaDTO> findAll() {
        return repository.findByActivoTrueOrderByCategoriaAscNombreAsc()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<TemplateConsultaDTO> findByCategoria(String categoria) {
        return repository.findByCategoriaAndActivoTrueOrderByNombreAsc(categoria)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<TemplateConsultaDTO> search(String nombre) {
        return repository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public TemplateConsultaDTO findById(Long id) {
        TemplateConsulta template = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template no encontrado"));
        return toDTO(template);
    }

    public TemplateConsultaDTO create(TemplateConsultaDTO dto, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        TemplateConsulta template = TemplateConsulta.builder()
            .nombre(dto.getNombre())
            .descripcion(dto.getDescripcion())
            .categoria(dto.getCategoria())
            .examenFisico(dto.getExamenFisico())
            .diagnostico(dto.getDiagnostico())
            .tratamiento(dto.getTratamiento())
            .observaciones(dto.getObservaciones())
            .activo(true)
            .creadoPor(usuario)
            .vecesUsado(0)
            .build();

        template = repository.save(template);
        log.info("Template de consulta creado: {}", template.getId());
        return toDTO(template);
    }

    public TemplateConsultaDTO update(Long id, TemplateConsultaDTO dto) {
        TemplateConsulta template = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template no encontrado"));

        template.setNombre(dto.getNombre());
        template.setDescripcion(dto.getDescripcion());
        template.setCategoria(dto.getCategoria());
        template.setExamenFisico(dto.getExamenFisico());
        template.setDiagnostico(dto.getDiagnostico());
        template.setTratamiento(dto.getTratamiento());
        template.setObservaciones(dto.getObservaciones());
        if (dto.getActivo() != null) {
            template.setActivo(dto.getActivo());
        }

        template = repository.save(template);
        log.info("Template de consulta actualizado: {}", id);
        return toDTO(template);
    }

    public void delete(Long id) {
        TemplateConsulta template = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template no encontrado"));
        template.setActivo(false);
        repository.save(template);
        log.info("Template de consulta desactivado: {}", id);
    }

    public void incrementarUso(Long id) {
        TemplateConsulta template = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template no encontrado"));
        template.setVecesUsado(template.getVecesUsado() + 1);
        repository.save(template);
    }

    private TemplateConsultaDTO toDTO(TemplateConsulta template) {
        return TemplateConsultaDTO.builder()
            .id(template.getId())
            .nombre(template.getNombre())
            .descripcion(template.getDescripcion())
            .categoria(template.getCategoria())
            .examenFisico(template.getExamenFisico())
            .diagnostico(template.getDiagnostico())
            .tratamiento(template.getTratamiento())
            .observaciones(template.getObservaciones())
            .activo(template.getActivo())
            .usuarioId(template.getCreadoPor() != null ? template.getCreadoPor().getId() : null)
            .usuarioNombre(template.getCreadoPor() != null ? template.getCreadoPor().getNombre() : null)
            .vecesUsado(template.getVecesUsado())
            .createdAt(template.getCreatedAt())
            .updatedAt(template.getUpdatedAt())
            .build();
    }
}

