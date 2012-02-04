/*
 Copyright 2008-2011 Gephi
 Authors : Taras Klaskovsky <megaterik@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 Gephi is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 Gephi is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.lsg_generator;

import cern.jet.random.engine.MersenneTwister;
import java.awt.RadialGradientPaint;
import java.security.SecureRandom;

public class DistributionGenerator{

    MersenneTwister randomFromColt;
    SecureRandom random;

    public DistributionGenerator() {
        randomFromColt = new MersenneTwister(new java.util.Date());
        random = new SecureRandom();
    }

    int nextPowerLaw(int min, int max, double power) {
        int res;
        do {
            res = (int) ((max - cern.jet.random.Distributions.nextPowLaw(power, max, randomFromColt)) + min);
        } while (res > max);
        return res;
    }
    
    int nextInt(int max)
    {
        return random.nextInt(max);
    }
}