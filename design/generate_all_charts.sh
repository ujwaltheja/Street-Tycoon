#!/bin/bash

# Street Tycoon - Generate All Visualization Charts
# This script generates all design charts for the project

echo "🎨 Street Tycoon Chart Generation"
echo "=================================="
echo ""

# Color codes for terminal
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Python is available
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}❌ Python 3 is not installed${NC}"
    exit 1
fi

# Check if required packages are installed
echo "Checking dependencies..."
python3 -c "import plotly, pandas, numpy, kaleido" 2>/dev/null
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Missing dependencies. Installing...${NC}"
    pip3 install -r requirements.txt
fi

echo -e "${GREEN}✓ Dependencies OK${NC}"
echo ""

# Generate each chart
charts=(
    "chart_architecture_enhanced.py:Enhanced Architecture"
    "chart_progression_curves.py:Progression Curves"
    "chart_game_balance.py:Game Balance"
    "chart_offline_accrual.py:Offline Accrual"
    "chart_stall_comparison.py:Stall Comparison"
    "chart_player_funnel.py:Player Funnel"
    "chart_monetization_flow.py:Monetization Flow"
    "chart_performance_benchmarks.py:Performance Benchmarks"
    "chart_localization_fixed.py:Localization Coverage"
    "chart_script_1.py:Monetization Models"
)

success_count=0
fail_count=0

for chart_info in "${charts[@]}"; do
    IFS=':' read -r script_name chart_name <<< "$chart_info"

    if [ -f "$script_name" ]; then
        echo "Generating: $chart_name..."
        python3 "$script_name" > /dev/null 2>&1

        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ $chart_name generated${NC}"
            ((success_count++))
        else
            echo -e "${RED}❌ $chart_name failed${NC}"
            ((fail_count++))
        fi
    else
        echo -e "${RED}⚠ $script_name not found${NC}"
        ((fail_count++))
    fi
done

echo ""
echo "=================================="
echo "Summary:"
echo -e "${GREEN}✓ Success: $success_count${NC}"
if [ $fail_count -gt 0 ]; then
    echo -e "${RED}✗ Failed: $fail_count${NC}"
fi
echo ""

# List generated files
echo "Generated files:"
ls -lh *.png *.svg 2>/dev/null | tail -n +2 | awk '{print "  " $9 " (" $5 ")"}'

echo ""
echo "🎉 Chart generation complete!"
