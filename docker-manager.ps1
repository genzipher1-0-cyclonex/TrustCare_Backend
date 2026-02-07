# Trust Care Docker Management Script for PowerShell

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Trust Care Docker Management" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Function to display menu
function Show-Menu {
    Write-Host "Select an option:" -ForegroundColor Yellow
    Write-Host "1. Start all services (database + backend)"
    Write-Host "2. Stop all services"
    Write-Host "3. View logs (all services)"
    Write-Host "4. View backend logs only"
    Write-Host "5. View database logs only"
    Write-Host "6. Rebuild and restart"
    Write-Host "7. Stop and remove all (including data)"
    Write-Host "8. Check service status"
    Write-Host "9. Access database shell"
    Write-Host "0. Exit"
    Write-Host ""
}

do {
    Show-Menu
    $choice = Read-Host "Enter your choice"
    Write-Host ""

    switch ($choice) {
        "1" {
            Write-Host "Building and starting all services..." -ForegroundColor Green
            docker-compose up -d --build
            Write-Host ""
            Write-Host "Services started! Backend: http://localhost:8080" -ForegroundColor Green
        }
        "2" {
            Write-Host "Stopping all services..." -ForegroundColor Yellow
            docker-compose down
            Write-Host "Services stopped." -ForegroundColor Green
        }
        "3" {
            Write-Host "Viewing logs (Press Ctrl+C to exit)..." -ForegroundColor Green
            docker-compose logs -f
        }
        "4" {
            Write-Host "Viewing backend logs (Press Ctrl+C to exit)..." -ForegroundColor Green
            docker-compose logs -f backend
        }
        "5" {
            Write-Host "Viewing database logs (Press Ctrl+C to exit)..." -ForegroundColor Green
            docker-compose logs -f postgres
        }
        "6" {
            Write-Host "Rebuilding and restarting..." -ForegroundColor Yellow
            docker-compose up -d --build
            Write-Host "Services rebuilt and restarted!" -ForegroundColor Green
        }
        "7" {
            Write-Host "WARNING: This will delete all database data!" -ForegroundColor Red
            $confirm = Read-Host "Are you sure? (yes/no)"
            if ($confirm -eq "yes") {
                docker-compose down -v
                Write-Host "All services and data removed." -ForegroundColor Green
            } else {
                Write-Host "Operation cancelled." -ForegroundColor Yellow
            }
        }
        "8" {
            Write-Host "Service Status:" -ForegroundColor Green
            docker-compose ps
        }
        "9" {
            Write-Host "Accessing database shell..." -ForegroundColor Green
            Write-Host "Use '\q' to exit the database shell" -ForegroundColor Yellow
            docker exec -it trust_care_db psql -U trust_care_user -d trust_care_db
        }
        "0" {
            Write-Host "Exiting..." -ForegroundColor Green
            break
        }
        default {
            Write-Host "Invalid choice. Please try again." -ForegroundColor Red
        }
    }

    Write-Host ""
    Write-Host "Press any key to continue..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    Clear-Host

} while ($choice -ne "0")
