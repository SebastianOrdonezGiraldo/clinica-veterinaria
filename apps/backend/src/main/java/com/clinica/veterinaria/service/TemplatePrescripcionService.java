package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.TemplatePrescripcionDTO;
import com.clinica.veterinaria.dto.TemplatePrescripcionItemDTO;
import com.clinica.veterinaria.entity.TemplatePrescripcion;
import com.clinica.veterinaria.entity.TemplatePrescripcionItem;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.TemplatePrescripcionRepository;
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
public class TemplatePrescripcionService {

    private final TemplatePrescripcionRepository repository;
    private final UsuarioRepository usuarioRepository;

    public List<TemplatePrescripcionDTO> findAll() {
        return repository.findByActivoTrueOrderByCategoriaAscNombreAsc()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<TemplatePrescripcionDTO> findByCategoria(String categoria) {
        return repository.findByCategoriaAndActivoTrueOrderByNombreAsc(categoria)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<String> getCategorias() {
        return repository.findDistinctCategorias();
    }

    public List<TemplatePrescripcionDTO> search(String nombre) {
        return repository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public TemplatePrescripcionDTO findById(Long id) {
        TemplatePrescripcion template = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template no encontrado"));
        return toDTO(template);
    }

    public TemplatePrescripcionDTO create(TemplatePrescripcionDTO dto, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        TemplatePrescripcion template = TemplatePrescripcion.builder()
            .nombre(dto.getNombre())
            .descripcion(dto.getDescripcion())
            .categoria(dto.getCategoria())
            .indicacionesGenerales(dto.getIndicacionesGenerales())
            .activo(true)
            .creadoPor(usuario)
            .vecesUsado(0)
            .build();

        // Agregar items
        if (dto.getItems() != null) {
            for (int i = 0; i < dto.getItems().size(); i++) {
                TemplatePrescripcionItemDTO itemDTO = dto.getItems().get(i);
                TemplatePrescripcionItem item = TemplatePrescripcionItem.builder()
                    .medicamento(itemDTO.getMedicamento())
                    .presentacion(itemDTO.getPresentacion())
                    .dosis(itemDTO.getDosis())
                    .frecuencia(itemDTO.getFrecuencia())
                    .duracion(itemDTO.getDuracion())
                    .indicaciones(itemDTO.getIndicaciones())
                    .orden(i)
                    .template(template)
                    .build();
                template.getItems().add(item);
            }
        }

        template = repository.save(template);
        log.info("Template de prescripción creado: {}", template.getId());
        return toDTO(template);
    }

    public TemplatePrescripcionDTO update(Long id, TemplatePrescripcionDTO dto) {
        TemplatePrescripcion template = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template no encontrado"));

        template.setNombre(dto.getNombre());
        template.setDescripcion(dto.getDescripcion());
        template.setCategoria(dto.getCategoria());
        template.setIndicacionesGenerales(dto.getIndicacionesGenerales());
        if (dto.getActivo() != null) {
            template.setActivo(dto.getActivo());
        }

        // Actualizar items
        template.getItems().clear();
        if (dto.getItems() != null) {
            for (int i = 0; i < dto.getItems().size(); i++) {
                TemplatePrescripcionItemDTO itemDTO = dto.getItems().get(i);
                TemplatePrescripcionItem item = TemplatePrescripcionItem.builder()
                    .medicamento(itemDTO.getMedicamento())
                    .presentacion(itemDTO.getPresentacion())
                    .dosis(itemDTO.getDosis())
                    .frecuencia(itemDTO.getFrecuencia())
                    .duracion(itemDTO.getDuracion())
                    .indicaciones(itemDTO.getIndicaciones())
                    .orden(i)
                    .template(template)
                    .build();
                template.getItems().add(item);
            }
        }

        template = repository.save(template);
        log.info("Template de prescripción actualizado: {}", id);
        return toDTO(template);
    }

    public void delete(Long id) {
        TemplatePrescripcion template = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template no encontrado"));
        template.setActivo(false);
        repository.save(template);
        log.info("Template de prescripción desactivado: {}", id);
    }

    public void incrementarUso(Long id) {
        TemplatePrescripcion template = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template no encontrado"));
        template.setVecesUsado(template.getVecesUsado() + 1);
        repository.save(template);
    }

    private TemplatePrescripcionDTO toDTO(TemplatePrescripcion template) {
        return TemplatePrescripcionDTO.builder()
            .id(template.getId())
            .nombre(template.getNombre())
            .descripcion(template.getDescripcion())
            .categoria(template.getCategoria())
            .indicacionesGenerales(template.getIndicacionesGenerales())
            .activo(template.getActivo())
            .usuarioId(template.getCreadoPor() != null ? template.getCreadoPor().getId() : null)
            .usuarioNombre(template.getCreadoPor() != null ? template.getCreadoPor().getNombre() : null)
            .vecesUsado(template.getVecesUsado())
            .items(template.getItems().stream()
                .sorted((a, b) -> Integer.compare(a.getOrden(), b.getOrden()))
                .map(item -> TemplatePrescripcionItemDTO.builder()
                    .id(item.getId())
                    .medicamento(item.getMedicamento())
                    .presentacion(item.getPresentacion())
                    .dosis(item.getDosis())
                    .frecuencia(item.getFrecuencia())
                    .duracion(item.getDuracion())
                    .indicaciones(item.getIndicaciones())
                    .orden(item.getOrden())
                    .createdAt(item.getCreatedAt())
                    .build())
                .collect(Collectors.toList()))
            .createdAt(template.getCreatedAt())
            .updatedAt(template.getUpdatedAt())
            .build();
    }
}

