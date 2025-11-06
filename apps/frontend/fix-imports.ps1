# Script para corregir imports a la nueva estructura

$files = Get-ChildItem -Path "src" -Recurse -Include "*.tsx","*.ts"

foreach ($file in $files) {
    $content = Get-Content -Path $file.FullName -Raw
    $originalContent = $content
    
    # Actualizar imports de componentes UI
    $content = $content -replace "@/components/ui/", "@shared/components/ui/"
    
    # Actualizar imports de layout
    $content = $content -replace "@/components/layout/", "@shared/components/layout/"
    
    # Actualizar imports de componentes comunes  
    $content = $content -replace "@/components/", "@shared/components/common/"
    
    # Actualizar imports de lib
    $content = $content -replace "@/lib/", "@shared/utils/"
    
    # Actualizar imports de types
    $content = $content -replace "from '@/types'", "from '@core/types'"
    
    # Actualizar imports de contexts
    $content = $content -replace "@/contexts/", "@core/auth/"
    
    # Actualizar imports de hooks
    $content = $content -replace "@/hooks/", "@shared/hooks/"
    
    # Solo escribir si hubo cambios
    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        Write-Host "Actualizado: $($file.Name)"
    }
}

Write-Host "Todos los imports actualizados!"
